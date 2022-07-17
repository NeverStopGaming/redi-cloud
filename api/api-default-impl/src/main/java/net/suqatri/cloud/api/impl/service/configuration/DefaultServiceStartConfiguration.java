package net.suqatri.cloud.api.impl.service.configuration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import net.suqatri.cloud.api.CloudAPI;
import net.suqatri.cloud.api.group.ICloudGroup;
import net.suqatri.cloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.cloud.api.service.ServiceEnvironment;
import net.suqatri.cloud.api.service.configuration.IServiceStartConfiguration;
import net.suqatri.cloud.commons.function.future.FutureAction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Getter @Setter
public class DefaultServiceStartConfiguration implements IServiceStartConfiguration {

    private ServiceEnvironment environment;
    private String javaCommand;
    private String name;
    private UUID uniqueId;
    private int id;
    private int maxMemory;
    private Collection<UUID> possibleNodeIds;
    private int startPriority;
    private boolean isStatic;
    private Collection<String> templateNames = new ArrayList<>();
    private String groupName;
    private List<String> processParameters;
    private List<String> jvmArguments;
    private boolean hasGroup;
    private int startPort;
    private String serviceVersionName;
    private UUID nodeId;

    public static FutureAction<DefaultServiceStartConfiguration> fromInterface(IServiceStartConfiguration interfaceConfig){
        FutureAction<DefaultServiceStartConfiguration> futureAction = new FutureAction<>();
        DefaultServiceStartConfiguration configuration = new DefaultServiceStartConfiguration();
        configuration.setEnvironment(interfaceConfig.getEnvironment());
        configuration.setJavaCommand(interfaceConfig.getJavaCommand());
        configuration.setName(interfaceConfig.getName());
        configuration.setUniqueId(interfaceConfig.getUniqueId());
        configuration.setId(interfaceConfig.getId());
        configuration.setMaxMemory(interfaceConfig.getMaxMemory());
        configuration.setPossibleNodeIds(interfaceConfig.getPossibleNodeIds());
        configuration.setStartPriority(interfaceConfig.getStartPriority());
        configuration.setStatic(interfaceConfig.isStatic());
        configuration.setTemplateNames(interfaceConfig.getTemplateNames());
        configuration.setProcessParameters(interfaceConfig.getProcessParameters());
        configuration.setJvmArguments(interfaceConfig.getJvmArguments());
        configuration.setTemplateNames(interfaceConfig.getTemplateNames());
        configuration.setServiceVersionName(interfaceConfig.getServiceVersionName());

        if(interfaceConfig.getGroupName() != null){
            configuration.setHasGroup(true);
            configuration.setGroupName(interfaceConfig.getGroupName());
            CloudAPI.getInstance().getGroupManager().getGroupAsync(interfaceConfig.getGroupName())
                        .onFailure(futureAction)
                        .onSuccess(groupHolder -> {
                            configuration.getTemplateNames().addAll(groupHolder.get().getTemplateNames());
                            futureAction.complete(configuration);
                        });
        }else{
            configuration.setHasGroup(false);
            futureAction.complete(configuration);
        }

        return futureAction;
    }


    @Override
    public boolean isGroupBased() {
        return this.hasGroup;
    }
}
