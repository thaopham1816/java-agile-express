package com.express.domain;

import com.express.testutils.SetterGetterInvoker;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.unitils.UnitilsJUnit4;

import static junit.framework.Assert.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class ProjectTest extends UnitilsJUnit4 {
   private static final Log LOG = LogFactory.getLog(ProjectTest.class);
   
   Project project;

   @Before
   public void setUp() {
      project = new Project();
      LOG.info(project);
   }
   
   @Test
   public void shouldSetAndGetProperties() {
       SetterGetterInvoker<Project> setterGetterInvoker = new SetterGetterInvoker<Project>(new Project());
       setterGetterInvoker.invokeSettersAndGetters();
   }

   @Test
   public void shouldAddAndRemoveDevelopers() {
      assertNotNull(project.getProjectWorkers());
      assertEquals(0, project.getProjectWorkers().size());
      ProjectWorker projectWorker = new ProjectWorker();
      project.addProjectWorker(projectWorker);
      assertEquals(1, project.getProjectWorkers().size());
      project.removeProjectWorker(projectWorker);
      assertEquals(0, project.getProjectWorkers().size());
   }

   @Test
   public void shouldAddAndRemoveAccessRequests() {
      assertNotNull(project.getAccessRequests());
      assertEquals(0, project.getAccessRequests().size());
      AccessRequest request = new AccessRequest();
      project.addAccessRequest(request);
      assertEquals(1, project.getAccessRequests().size());
      assertEquals(project, request.getProject());
      project.removeAccessRequest(request);
      assertEquals(0, project.getAccessRequests().size());
      assertNull(request.getProject());
   }
   
   @Test
   public void shouldAddAndRemoveThemes() {
      assertNotNull(project.getThemes());
      assertEquals(0, project.getThemes().size());
      Theme theme = new Theme();
      project.addTheme(theme);
      assertEquals(1, project.getThemes().size());
      assertEquals(project, theme.getProject());
      project.removeTheme(theme);
      assertEquals(0, project.getThemes().size());
      assertNull(theme.getProject());
   }

   @Test
   public void projectWorkersWithProjectAdminPermissionShouldBeReturnedAsManagers() {
      int numberOfAdmins = 1;
      assertNotNull(project.getProjectWorkers());
      assertEquals(0, project.getProjectWorkers().size());
      ProjectWorker projectWorker = new ProjectWorker();
      projectWorker.getPermissions().setProjectAdmin(true);
      project.addProjectWorker(projectWorker);
      //Add non-manager
      projectWorker = new ProjectWorker();
      project.addProjectWorker(projectWorker);
      assertThat("number of admins", project.getProjectManagers().size(), equalTo(numberOfAdmins));
   }

   @Test
   public void shouldIncrementStoryCountWhenAddedToProductBacklog() {
      assertEquals(0, project.getStoryCount().intValue());
      project.addBacklogItem(new BacklogItem(), true);
      assertEquals(1, project.getStoryCount().intValue());
   }

   @Test
   public void shouldNotIncrementStoryCountWhenMovedToProductBacklog() {
      assertEquals(0, project.getStoryCount().intValue());
      project.addBacklogItem(new BacklogItem(), false);
      assertEquals(0, project.getStoryCount().intValue());
   }

   @Test
   public void shouldIncrementStoryCountWhenAddedToIterationBacklog() {
      assertEquals(0, project.getStoryCount().intValue());
      Iteration iteration = new Iteration();
      project.addIteration(iteration);
      iteration.addBacklogItem(new BacklogItem(), true);
      assertEquals(1, project.getStoryCount().intValue());
   }

   @Test
   public void shouldNotIncrementStoryCountWhenMovedToIterationBacklog() {
      assertEquals(0, project.getStoryCount().intValue());
      Iteration iteration = new Iteration();
      project.addIteration(iteration);
      iteration.addBacklogItem(new BacklogItem(), false);
      assertEquals(0, project.getStoryCount().intValue());
   }

}
