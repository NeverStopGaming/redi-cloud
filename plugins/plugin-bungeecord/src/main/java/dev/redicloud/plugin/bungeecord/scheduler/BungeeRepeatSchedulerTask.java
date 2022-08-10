package dev.redicloud.plugin.bungeecord.scheduler;

import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import dev.redicloud.api.scheduler.IRepeatScheduler;
import dev.redicloud.api.scheduler.ITaskFilter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class BungeeRepeatSchedulerTask implements IRepeatScheduler<ScheduledTask> {

    private final List<ITaskFilter> filters;
    private boolean asyncFilter;
    private ScheduledTask task;
    private BungeeScheduler scheduler;
    private int id;

    public BungeeRepeatSchedulerTask(BungeeScheduler scheduler) {
        this.scheduler = scheduler;
        this.filters = new ArrayList<>();
        this.id = -1;
    }

    @Override
    public void setTask(ScheduledTask task) {
        this.task = task;
        this.id = task.getId();
    }

    @Override
    public void cancel() {
        if (this.task == null) return;
        this.task.cancel();
    }
}
