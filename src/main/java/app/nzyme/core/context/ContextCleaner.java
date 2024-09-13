package app.nzyme.core.context;

import app.nzyme.core.periodicals.Periodical;
import org.joda.time.DateTime;

public class ContextCleaner extends Periodical {

    private final ContextService contextService;

    public ContextCleaner(ContextService contextService) {
        this.contextService = contextService;
    }

    @Override
    protected void execute() {
        contextService.retentionCleanTransparentMacContext(DateTime.now().minusHours(24));
    }

    @Override
    public String getName() {
        return "ContextCleaner";
    }

}
