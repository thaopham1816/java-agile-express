package com.express.model
{
import com.express.model.domain.AccessRequest;
import com.express.model.domain.BacklogItem;
import com.express.model.domain.DailyIterationStatusRecord;
import com.express.model.domain.DailyProjectStatusRecord;
import com.express.model.domain.Issue;
import com.express.model.domain.Iteration;
import com.express.model.domain.Project;

import com.express.model.domain.ProjectWorker;
import com.express.model.domain.User;

import mx.collections.ArrayCollection;
import mx.collections.HierarchicalData;

import mx.collections.Sort;
import mx.collections.SortField;

import org.puremvc.as3.patterns.proxy.Proxy;

public class ProjectProxy extends Proxy
{
   // Cannonical name of the Proxy
   public static const NAME:String = "ProjectProxy";

   public static const UNCOMMITED_TITLE : String = "Uncommited Backlog";
   public static const UNCOMMITED_ID :  Number = -1;
   public static const DEVELOPER : String = "Developer";
   public static const STORY : String = "Story";

   private var _projectList : ArrayCollection;
   private var _selectedProject : Project;

   private var _iterationList : ArrayCollection;
   private var _selectedIteration : Iteration;

   private var _selectedBacklog : HierarchicalData;
   private var _productBacklog : HierarchicalData;

   private var _developers : ArrayCollection;
   private var _nameSort : Sort;
   private var _projectWorkers : ArrayCollection;
   private var _accessRequests: ArrayCollection;
   private var _defectList : ArrayCollection;
   private var _burndown : ArrayCollection;
   private var _burnUp : ArrayCollection;
   private var _iterationDays : ArrayCollection;
   private var _themes : ArrayCollection;

   public var newImpediment : Issue;

   public var selectedBacklogItem : BacklogItem;
   public var selectedAccessRequest : AccessRequest;
   public var newIteration : Iteration;
   public var colourGroupings : ArrayCollection;
   public var productBacklogRequest : Boolean = false;

   public function ProjectProxy()
   {
      super(NAME, null);
      _projectList = new ArrayCollection();
      _iterationList = new ArrayCollection();
      _accessRequests = new ArrayCollection();

      _developers = new ArrayCollection();
      _nameSort = new Sort();
      _nameSort.fields = [new SortField("fullName", true)];
      _developers.sort = _nameSort;

      _projectWorkers = new ArrayCollection();
      _burndown = new ArrayCollection();
      _burnUp = new ArrayCollection();
      var burndownSort : Sort = new Sort();
      burndownSort.fields = [new SortField("date")];
      burndownSort.compareFunction =  sortOnDate;
      _burndown.sort = burndownSort;
      _iterationDays = new ArrayCollection();

      _selectedBacklog = new HierarchicalData();
      _selectedBacklog.childrenField = "tasks";

      _productBacklog = new HierarchicalData();
      _productBacklog.childrenField = "tasks";

      _defectList = new ArrayCollection();
      _themes = new ArrayCollection();

      colourGroupings = new ArrayCollection();
      colourGroupings.addItem(DEVELOPER);
      colourGroupings.addItem(STORY);
   }

   private function sortOnDate(a:Object, b:Object, fields:Array = null):int {
      if (a.date.getTime() < b.date.getTime()) {
         return -1;
      }
      if (a.date.getTime() > b.date.getTime()) {
         return 1;
      }
      return 0;
   }

   public function get projectList() : ArrayCollection {
      return _projectList;
   }

   public function set projectList(projectList : ArrayCollection) : void {
      _projectList.source = projectList.source;
   }

   public function addToProjectList(project : Project) : void {
      for each(var lstProject : Project in _projectList) {
         if(lstProject.id == project.id) {
            lstProject.copyFrom(project);
            return;
         }
      }
      _projectList.addItem(project);
   }

   public function get iterationList() : ArrayCollection {
      return _iterationList;
   }

   public function get defectList() : ArrayCollection {
      if (selectedProject == null) {
         return new ArrayCollection();
      }

      return selectedProject.defects;
   }

   public function set selectedIteration(iteration : Iteration) : void {
      _selectedIteration = iteration;
      if (iteration != null) {
         _selectedBacklog.source = iteration.backlog;
         setBurndown(iteration);
      }
      else {
         _selectedBacklog.source = [];
         _burndown.source = [];
      }
   }

   public function updateIterationList() : void {
      for each(var iteration : Iteration in _selectedProject.iterations) {
         var oldVersion : Iteration = getIteration(iteration.id);
         if (oldVersion) {
            oldVersion.copyFrom(iteration);
         }
         else {
            _iterationList.addItem(iteration);
         }
      }
   }

   private function getIteration(id : Number) : Iteration {
      for each(var iteration : Iteration in _iterationList) {
         if (iteration.id == id) {
            return iteration;
         }
      }
      return null;
   }

   public function get selectedIteration() : Iteration {
      return _selectedIteration;
   }

   [Bindable]
   public function get selectedProject() : Project {
      return _selectedProject;
   }

   public function set selectedProject(project : Project) : void {
      if(_selectedProject == null || project.id != _selectedProject.id) {
         _selectedProject = project;
         _iterationList.source = project.iterations == null ? [] : project.iterations.source;
      }
      else {
         _selectedProject.copyFrom(project);
         updateIterationList();

      }
      addToProjectList(project);
      accessRequests = project.accessRequests;
      _projectWorkers.source = project.projectWorkers.source;
      setDevelopers(project.projectWorkers);
      setProductBacklogSource(project.productBacklog);
      setBurnUp(project);
      themes = project.themes;
   }

   public function get selectedBacklog() : HierarchicalData {
      return _selectedBacklog;
   }

   public function get productBacklog() : HierarchicalData {
      return _productBacklog;
   }

   public function setProductBacklogSource(backlog : ArrayCollection) : void {
      _productBacklog.source = backlog;
   }

   public function get accessRequests() : ArrayCollection {
      return _accessRequests;
   }

   public function set accessRequests(accessRequests : ArrayCollection) : void {
      _accessRequests.source = [];
      for each(var accessRequest : AccessRequest in accessRequests) {
         if(accessRequest.status == AccessRequest.UNRESOLVED) {
            _accessRequests.addItem(accessRequest);
         }
      }
   }

   public function get developers() : ArrayCollection {
      return _developers;
   }

   public function setDevelopers(projectWorkers : ArrayCollection) : void {
      _developers.source = [];
      for each(var worker : ProjectWorker in projectWorkers) {
         _developers.addItem(worker.worker);
      }
      _developers.refresh();
   }

   public function getDeveloperIndex(developer : User) : int {
      if(developer == null) {
         return -1;
      }
      var index : int;
      for each(var dev : User in _developers) {
         if(dev.id == developer.id) {
            return index;
         }
         index++;
      }
      return -1;
   }

   public function get burndown() : ArrayCollection {
      return _burndown;
   }

   public function setBurndown(iteration : Iteration) : void {
      if(iteration) {
         _burndown.source = iteration.history.source.concat();
         if(iteration.isOpen()) {
            var effort : DailyIterationStatusRecord = new DailyIterationStatusRecord();
            var temp: Date = new Date();
            effort.date = new Date(temp.getFullYear(), temp.getMonth(), temp.getDate());
            effort.taskHoursRemaining = iteration.getTaskHoursRemaining();
            _burndown.addItem(effort);
         }
      }
      else {
         _burndown.source = [];
      }
      for each(var record : DailyIterationStatusRecord in _burndown) {
            trace(record.id + ":" + record.date);
         }
   }

   public function get burnUp() : ArrayCollection {
      return _burnUp;
   }

   public function setBurnUp(project : Project) : void {
      if(project.history) {
         _burnUp.source = project.history.source.concat();
//         var finalRecord : DailyProjectStatusRecord;
//         if(_burnUp.length > 0) {
//            finalRecord = DailyProjectStatusRecord(burnUp.getItemAt(_burnUp.length - 1));
//         }
//         if(!finalRecord || finalRecord.date.getTime() < project.targetReleaseDate.getTime() ) {
//            var effort : DailyProjectStatusRecord = new DailyProjectStatusRecord();
//            effort.date = project.targetReleaseDate;
//            effort.totalPoints = finalRecord.totalPoints;
//            _burnUp.addItem(effort);
//         }
      }
      else {
         _burndown.source = [];
      }
      for each(var record : DailyIterationStatusRecord in _burndown) {
            trace(record.id + ":" + record.date);
         }
   }

   public function get iterationDays() : ArrayCollection {
      return _iterationDays;
   }

   public function set themes(themes : ArrayCollection) : void {
      if(themes) {
         _themes.source = themes.source.concat();
      }
   }

   public function get  themes() : ArrayCollection {
      return _themes;
   }

   public function updateCurrentUser(user : User) : void {
      for each(var developer : User in _developers) {
         if(developer.id == user.id) {
            developer.copyFrom(user);
            return;
         }
      }
   }

   public function get projectWorkers() : ArrayCollection {
      return _projectWorkers;
   }

   public function removeProjectWorker(worker : ProjectWorker) : void {

      for(var index : int = 0; index < _projectWorkers.length ; index++) {
         if(_projectWorkers.getItemAt(index).id == worker.id) {
            _projectWorkers.removeItemAt(index);
            return;
         }
      }
   }

}
}