package moe.ichinomiya.naven.commands;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public abstract class Command {
    @Getter
    private String name;

    @Getter
    private String description;

    @Getter
    private String[] aliases;

    protected void initCommand() {
        if (this.getClass().isAnnotationPresent(CommandInfo.class)) {
            CommandInfo commandInfo = this.getClass().getAnnotation(CommandInfo.class);

            this.name = commandInfo.name();
            this.description = commandInfo.description();
            this.aliases = commandInfo.aliases();
        }
    }

    public abstract void onCommand(String[] args);

    public abstract String[] onTab(String[] args);
}
