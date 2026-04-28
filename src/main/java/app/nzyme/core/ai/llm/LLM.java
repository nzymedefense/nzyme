package app.nzyme.core.ai.llm;

import app.nzyme.core.NzymeNode;
import com.github.tjake.jlama.model.AbstractModel;
import com.github.tjake.jlama.model.ModelSupport;
import com.github.tjake.jlama.model.functions.Generator;
import com.github.tjake.jlama.safetensors.DType;
import com.github.tjake.jlama.safetensors.prompt.PromptContext;
import jakarta.validation.constraints.NotNull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Map;

public class LLM {

    private static final Logger LOG = LogManager.getLogger(LLM.class);

    private static final Pattern SAFE_PROMPT_FILENAME = Pattern.compile("^[a-zA-Z0-9_]+\\.md$");
    private static final Pattern PLACEHOLDER = Pattern.compile("\\{([A-Z0-9_]+)}");

    private final NzymeNode nzyme;
    private final File modelDirectory;

    private AbstractModel model;

    public LLM(NzymeNode nzyme) {
        this.nzyme = nzyme;

        this.modelDirectory = new File(nzyme.getBaseConfiguration().dataDirectory(), "llm");

        if (!modelDirectory.isDirectory()) {
            throw new RuntimeException("Model directory is not a " +
                    "directory: [" + this.modelDirectory.getAbsolutePath() + "].");
        }


        for (String f : new String[] { "config.json", "tokenizer.json" }) {
            if (!new File(this.modelDirectory, f).isFile()) {
                throw new RuntimeException("Model directory corrupted. Missing: [" + f + "].");
            }
        }

        try {
            try (var stream = Files.list(this.modelDirectory.toPath())) {
                if (stream.noneMatch(p -> p.getFileName().toString().endsWith(".safetensors"))) {
                    throw new RuntimeException("Model directory corrupted. Missing weights.");
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not list files in model " +
                    "directory: [" + this.modelDirectory.getAbsolutePath() + "].");
        }
    }

    public void initialize() {
        this.model = ModelSupport.loadModel(this.modelDirectory, DType.F32, DType.I8);
    }

    public void warmup() {
        PromptContext warmup = model.promptSupport().get().builder()
                .addUserMessage("Hello.")
                .build();
        model.generate(UUID.randomUUID(), warmup, 0.0f, 32, (s, f) -> {});
    }

    public Generator.Response query(@NotNull String prompt, @NotNull String systemMessage, int maxTokens) {
        if (model.promptSupport().isEmpty()) {
            throw new RuntimeException("Model has no prompt support.");
        }

        PromptContext context = model.promptSupport()
                .get()
                .builder()
                .addSystemMessage(systemMessage)
                .addUserMessage(prompt)
                .build();

        LOG.info("PROMPT: {}", context.getPrompt());

        int totalTokenEstimate = (prompt.length() + systemMessage.length()) / 3;
        return model.generate(UUID.randomUUID(), context, 0.3f, totalTokenEstimate + maxTokens, (s, f) -> {});
    }

    public String getSystemPrompt() {
        try {
            return Files.readString(new File(getPromptDirectory(), "system.md").toPath());
        } catch (IOException e) {
            throw new RuntimeException("Could not read system prompt file.", e);
        }
    }

    public Optional<String> getPrompt(String prompt, Map<String, String> params) {
        if (!SAFE_PROMPT_FILENAME.matcher(prompt).matches()) {
            throw new IllegalArgumentException("Illegal prompt filename: " + prompt);
        }

        File promptFile = new File(getPromptDirectory(), prompt);

        if (!promptFile.exists()) {
            return Optional.empty();
        }

        try {
            String template = Files.readString(promptFile.toPath());
            return Optional.of(replacePlaceholders(template, params));
        } catch (IOException e) {
            throw new RuntimeException("Could not read prompt file: [" + promptFile.getAbsolutePath() + "]", e);
        }
    }

    private String replacePlaceholders(String template, Map<String, String> params) {
        StringBuffer sb = new StringBuffer();
        Matcher matcher = PLACEHOLDER.matcher(template);

        while (matcher.find()) {
            String key = matcher.group(1);
            if (!params.containsKey(key)) {
                throw new IllegalArgumentException("No value provided for placeholder: {" + key + "}");
            }
            matcher.appendReplacement(sb, Matcher.quoteReplacement(params.get(key)));
        }

        matcher.appendTail(sb);
        return sb.toString();
    }

    private File getPromptDirectory() {
        File directory = new File(nzyme.getBaseConfiguration().dataDirectory(), "prompts");

        if (!directory.isDirectory()) {
            throw new RuntimeException("Prompt directory is not a " +
                    "directory: [" + directory.getAbsolutePath() + "].");
        }

        return directory;
    }

}
