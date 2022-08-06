package net.suqatri.redicloud.api.node.event;

import lombok.Data;
import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.event.CloudGlobalEvent;
import net.suqatri.redicloud.api.node.ICloudNode;
import net.suqatri.redicloud.commons.function.future.FutureAction;

import java.util.UUID;

@Data
public class CloudNodeEvent extends CloudGlobalEvent {

    private UUID nodeId;

    public ICloudNode getCloudNode() {
        return CloudAPI.getInstance().getNodeManager().getNode(this.nodeId);
    }

    public FutureAction<ICloudNode> getCloudNodeAsync() {
        return CloudAPI.getInstance().getNodeManager().getNodeAsync(this.nodeId);
    }

}
