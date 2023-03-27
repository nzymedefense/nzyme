package app.nzyme.core.rest.resources.system.cluster;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.rest.responses.distributed.TasksQueueTaskResponse;
import app.nzyme.core.rest.responses.distributed.TasksQueueTasksListResponse;
import app.nzyme.plugin.distributed.tasksqueue.StoredTask;
import app.nzyme.plugin.rest.security.RESTSecured;
import com.google.common.collect.Lists;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/api/system/cluster/tasksqueue")
@RESTSecured
@Produces(MediaType.APPLICATION_JSON)
public class TasksQueueResource {

    private static final Logger LOG = LogManager.getLogger(MessageBusResource.class);

    @Inject
    private NzymeNode nzyme;

    @GET
    @Path("/tasks")
    public Response findTasks(@QueryParam("limit") int limit, @QueryParam("offset") int offset) {
        if (limit > 250) {
            LOG.warn("Requested limit larger than 250. Not allowed.");
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        List<TasksQueueTaskResponse> tasks = Lists.newArrayList();
        for (StoredTask task : nzyme.getTasksQueue().getAllTasks(limit, offset)) {
            tasks.add(TasksQueueTaskResponse.create(
                    task.id(),
                    task.sender(),
                    task.type(),
                    task.allowRetry(),
                    task.createdAt(),
                    task.status(),
                    task.retries(),
                    task.allowProcessSelf(),
                    task.processingTimeMs(),
                    task.firstProcessedAt(),
                    task.lastProcessedAt(),
                    task.processedBy()
            ));
        }

        long count = nzyme.getTasksQueue().getTotalTaskCount();

        return Response.ok(TasksQueueTasksListResponse.create(count, tasks)).build();
    }

    @PUT
    @Path("/tasks/show/{id}/acknowledgefailure")
    public Response acknowledgeFailure(@PathParam("id") long id) {
        nzyme.getTasksQueue().acknowledgeTaskFailure(id);

        return Response.ok().build();
    }

}
