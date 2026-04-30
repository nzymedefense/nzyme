package app.nzyme.core.ai.llm;

import app.nzyme.core.NzymeNode;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.anthropic.AnthropicChatModel;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import jakarta.validation.constraints.NotNull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Map;

public class LLM {

    private static final Logger LOG = LogManager.getLogger(LLM.class);

    private static final Pattern SAFE_PROMPT_FILENAME = Pattern.compile("^[a-zA-Z0-9_]+\\.md$");
    private static final Pattern PLACEHOLDER = Pattern.compile("\\{([A-Z0-9_]+)}");

    private final NzymeNode nzyme;
    private ChatModel model;

    public LLM(NzymeNode nzyme) {
        this.nzyme = nzyme;
    }

    public void initialize() {
        this.model = AnthropicChatModel.builder()
                .apiKey(System.getenv("ANTHROPIC_API_KEY"))
                .modelName("claude-opus-4-7")
                .build();
    }

    public ChatResponse query(@NotNull String prompt, @NotNull String systemMessage) {
        return model.chat(List.of(SystemMessage.from(systemMessage), UserMessage.from(prompt)));
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
