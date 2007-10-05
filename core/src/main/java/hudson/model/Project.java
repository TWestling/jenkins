package hudson.model;

import hudson.model.Descriptor.FormException;
import hudson.tasks.BuildStep;
import hudson.tasks.BuildTrigger;
import hudson.tasks.BuildWrapper;
import hudson.tasks.BuildWrappers;
import hudson.tasks.Builder;
import hudson.tasks.Fingerprinter;
import hudson.tasks.Publisher;
import hudson.triggers.Trigger;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.*;

/**
 * Buildable software project.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class Project<P extends Project<P,B>,B extends Build<P,B>>
    extends AbstractProject<P,B> implements SCMedItem {

    /**
     * List of active {@link Builder}s configured for this project.
     */
    private volatile List<Builder> builders = new Vector<Builder>();

    /**
     * List of active {@link Publisher}s configured for this project.
     */
    private volatile List<Publisher> publishers = new Vector<Publisher>();

    /**
     * List of active {@link BuildWrapper}s configured for this project.
     */
    private volatile List<BuildWrapper> buildWrappers = new Vector<BuildWrapper>();

    /**
     * Creates a new project.
     */
    public Project(ItemGroup parent,String name) {
        super(parent,name);
    }

    public void onLoad(ItemGroup<? extends Item> parent, String name) throws IOException {
        super.onLoad(parent, name);

        if(buildWrappers==null)
            // it didn't exist in < 1.64
            buildWrappers = new Vector<BuildWrapper>();
    }

    public AbstractProject<?, ?> asProject() {
        return this;
    }

    public Map<Descriptor<Builder>,Builder> getBuilders() {
        return Descriptor.toMap(builders);
    }

    public Map<Descriptor<Publisher>,Publisher> getPublishers() {
        return Descriptor.toMap(publishers);
    }

    public Map<Descriptor<BuildWrapper>,BuildWrapper> getBuildWrappers() {
        return Descriptor.toMap(buildWrappers);
    }

    /** {@inheritDoc} */
    @Override
    protected Set<ResourceActivity> getResourceActivities() {
        final Set<ResourceActivity> activities = new HashSet<ResourceActivity>();
        activities.addAll(super.getResourceActivities());
        for (Builder builder : builders) {
            if (builder instanceof ResourceActivity) {
                activities.add(ResourceActivity.class.cast(builder));
            }
        }
        for (Publisher publisher : publishers) {
            if (publisher instanceof ResourceActivity) {
                activities.add(ResourceActivity.class.cast(publisher));
            }
        }
        for (BuildWrapper buildWrapper : buildWrappers) {
            if (buildWrapper instanceof ResourceActivity) {
                activities.add(ResourceActivity.class.cast(buildWrapper));
            }
        }
        return activities;
    }

    /**
     * Adds a new {@link BuildStep} to this {@link Project} and saves the configuration.
     */
    public void addPublisher(Publisher buildStep) throws IOException {
        addToList(buildStep,publishers);
    }

    /**
     * Removes a publisher from this project, if it's active.
     */
    public void removePublisher(Descriptor<Publisher> descriptor) throws IOException {
        removeFromList(descriptor, publishers);
    }

    public Publisher getPublisher(Descriptor<Publisher> descriptor) {
        for (Publisher p : publishers) {
            if(p.getDescriptor()==descriptor)
                return p;
        }
        return null;
    }

    protected void buildDependencyGraph(DependencyGraph graph) {
        BuildTrigger buildTrigger = (BuildTrigger) getPublishers().get(BuildTrigger.DESCRIPTOR);
        if(buildTrigger!=null)
             graph.addDependency(this,buildTrigger.getChildProjects());
    }

    @Override
    public boolean isFingerprintConfigured() {
        synchronized(publishers) {
            for (Publisher p : publishers) {
                if(p instanceof Fingerprinter)
                    return true;
            }
        }
        return false;
    }



//
//
// actions
//
//
    @Override
    protected void submit( StaplerRequest req, StaplerResponse rsp ) throws IOException, ServletException, FormException {
        super.submit(req,rsp);

        if(!Hudson.adminCheck(req,rsp))
            return;

        req.setCharacterEncoding("UTF-8");

        buildWrappers = buildDescribable(req, BuildWrappers.WRAPPERS, "wrapper");
        builders = buildDescribable(req, BuildStep.BUILDERS, "builder");
        publishers = buildDescribable(req, BuildStep.PUBLISHERS, "publisher");
        updateTransientActions(); // to pick up transient actions from builder, publisher, etc.
    }

    protected void updateTransientActions() {
        synchronized(transientActions) {
            super.updateTransientActions();

            for (BuildStep step : builders) {
                Action a = step.getProjectAction(this);
                if(a!=null)
                    transientActions.add(a);
            }
            for (BuildStep step : publishers) {
                Action a = step.getProjectAction(this);
                if(a!=null)
                    transientActions.add(a);
            }
            for (Trigger trigger : triggers) {
                Action a = trigger.getProjectAction();
                if(a!=null)
                    transientActions.add(a);
            }
        }
    }

    public List<ProminentProjectAction> getProminentActions() {
        List<Action> a = getActions();
        List<ProminentProjectAction> pa = new Vector<ProminentProjectAction>();
        for (Action action : a) {
            if(action instanceof ProminentProjectAction)
                pa.add((ProminentProjectAction) action);
        }
        return pa;
    }

    /**
     * @deprecated
     *      left for legacy config file compatibility
     */
    @Deprecated
    private transient String slave;
}