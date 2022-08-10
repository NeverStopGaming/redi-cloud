package dev.redicloud.api.impl.service.configuration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import dev.redicloud.api.group.ICloudGroup;
import dev.redicloud.api.service.ICloudService;
import dev.redicloud.api.service.ServiceEnvironment;
import dev.redicloud.api.service.configuration.IServiceStartConfiguration;
import dev.redicloud.commons.function.future.FutureAction;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class GroupServiceStartConfiguration implements IServiceStartConfiguration {

    private UUID uniqueId;
    private ServiceEnvironment environment;
    private String name;
    private int id = -1;
    private int maxMemory;
    private Collection<UUID> possibleNodeIds;
    private int startPriority = 0;
    private boolean staticService = false;
    private Collection<String> templateNames;
    private String groupName;
    private List<String> processParameters;
    private List<String> jvmArguments;
    private int startPort;
    private String serviceVersionName;
    private UUID nodeId;
    private boolean fallback = false;
    @JsonIgnore
    private FutureAction<ICloudService> startListener;
    private int percentToStartNewService;

    @Override
    public void listenToStart() {
        if (this.startListener != null) return;
        this.startListener = new FutureAction<>();
    }

    @Override
    public boolean isStatic() {
        return this.staticService;
    }

    public GroupServiceStartConfiguration applyFromGroup(ICloudGroup group) {
        this.uniqueId = UUID.randomUUID();
        this.environment = group.getServiceEnvironment();
        this.name = group.getName();
        this.percentToStartNewService = group.getPercentToStartNewService();
        this.maxMemory = group.getMaxMemory();
        this.possibleNodeIds = group.getAssociatedNodeIds();
        this.startPriority = group.getStartPriority();
        this.staticService = group.isStatic();
        this.templateNames = group.getTemplateNames();
        this.groupName = group.getName();
        this.processParameters = Arrays.asList(group.getProcessParameters());
        this.jvmArguments = Arrays.asList(group.getJvmArguments());
        this.startPort = group.getStartPort();
        this.serviceVersionName = group.getServiceVersionName();
        this.fallback = group.isFallback();
        return this;
    }

    @Override
    public boolean isGroupBased() {
        return true;
    }
}
