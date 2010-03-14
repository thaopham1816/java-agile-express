package com.express.domain;

import org.hibernate.annotations.OptimisticLock;

import javax.persistence.*;
import java.util.*;

/**
 * Models an Iteration which is effectively a work period for a project. an Iteration
 * primarily contains a backlog which is a list of Stories which need to be completed before completion.
 *
 * @author adam boas
 */
@Entity
@Table(name = "ITERATION")
@NamedQueries({
      @NamedQuery(name = "Iteration.findOpen", query = "SELECT I FROM Iteration I WHERE I.startDate <= :date AND I.endDate >= :date")
})
public class Iteration implements Persistable, Comparable<Iteration> {

   private static final long serialVersionUID = -4839071199885329170L;

   public static final String QUERY_FIND_OPEN = "Iteration.findOpen";

   @Id
   @GeneratedValue(strategy = GenerationType.TABLE, generator = "GEN_ITERATION")
   @TableGenerator(name = "GEN_ITERATION", table = "SEQUENCE_LIST", pkColumnName = "NAME",
         valueColumnName = "NEXT_VALUE", allocationSize = 1, initialValue = 100,
         pkColumnValue = "ITERATION")
   @Column(name = "ITERATION_ID")
   private Long id;

   @Version
   @Column(name = "VERSION_NO")
   private Long version;

   @Column(name = "START_DATE")
   @Temporal(value = TemporalType.TIMESTAMP)
   private Calendar startDate;

   @Column(name = "END_DATE")
   @Temporal(value = TemporalType.TIMESTAMP)
   private Calendar endDate;

   @Column(name = "TITLE")
   private String title;

   @Column(name = "DESCRIPTION")
   private String description;

   @Column(name = "FINAL_VELOCITY")
   private Integer finalVelocity;

   @ManyToOne
   @JoinColumn(name = "PROJECT_ID")
   @OptimisticLock(excluded = true)
   private Project project;

   @OneToMany(mappedBy = "iteration", cascade = CascadeType.ALL)
   @OptimisticLock(excluded = true)
   private Set<BacklogItem> backlog;

   @OneToMany(mappedBy = "iteration", cascade = CascadeType.ALL)
   @OptimisticLock(excluded = true)
   private Set<Issue> impediments;

   @OneToMany(mappedBy = "iteration", cascade = CascadeType.ALL)
   private Set<DailyIterationStatusRecord> history;

   public Iteration() {
      backlog = new HashSet<BacklogItem>();
      impediments = new HashSet<Issue>();
      history = new HashSet<DailyIterationStatusRecord>();
   }

   public Long getId() {
      return id;
   }

   public void setId(Long id) {
      this.id = id;
   }

   public Calendar getStartDate() {
      return startDate;
   }

   public void setStartDate(Calendar startDate) {
      this.startDate = startDate;
   }

   public Calendar getEndDate() {
      return endDate;
   }

   public void setEndDate(Calendar endDate) {
      this.endDate = endDate;
   }

   public String getTitle() {
      return title;
   }

   public void setTitle(String title) {
      this.title = title;
   }

   public String getDescription() {
      return description;
   }

   public void setDescription(String description) {
      this.description = description;
   }

   public Project getProject() {
      return project;
   }

   public void setProject(Project project) {
      this.project = project;
   }

   public Set<BacklogItem> getBacklog() {
      return this.backlog;
   }

   public void setBacklog(Set<BacklogItem> backlog) {
      this.backlog = backlog;
   }

   public Integer getFinalVelocity() {
      return finalVelocity;
   }

   public void setFinalVelocity(Integer finalVelocity) {
      this.finalVelocity = finalVelocity;
   }

   public void addBacklogItem(BacklogItem backlogItem, boolean isNew) {
      this.backlog.add(backlogItem);
      backlogItem.setIteration(this);
      backlogItem.setProject(null);
      if(isNew) {
         this.project.incrementStoryCount();
      }
   }

   public boolean removeBacklogItem(BacklogItem backlogItem) {
      boolean result = this.backlog.remove(backlogItem);
      if (result) {
         backlogItem.setIteration(null);
      }
      return result;
   }

   public Set<DailyIterationStatusRecord> getHistory() {
      return history;
   }

   public void setHistory(Set<DailyIterationStatusRecord> history) {
      this.history = history;
   }

   public void addHistoryRecord(DailyIterationStatusRecord record) {
      this.history.add(record);
   }

   public Set<Issue> getImpediments() {
      return impediments;
   }

   public void setImpediments(Set<Issue> impediments) {
      this.impediments = impediments;
   }

   public void addImpediment(Issue impediment) {
      this.impediments.add(impediment);
      impediment.setIteration(this);
   }

   public void removeImpediment(Issue impediment) {
      this.impediments.remove(impediment);
      impediment.setIteration(null);
   }

   public int compareTo(Iteration iteration) {
      return this.startDate.compareTo(iteration.getStartDate());
   }

   /**
    * Task effort is intended to change over time as tasks are completed or moved toward completion.
    * This method returns the current remaining hours of effort to complete this Iteration.
    *
    * @return int representing the current remaining hours of effort to complete this Iteration.
    */
   public int getTaskEffortRemaining() {
      int remainingHours = 0;
      for (BacklogItem item : backlog) {
         remainingHours += item.getTaskEffortRemaining();
      }
      return remainingHours;
   }

   /**
    * It is not intended that Velocity vary in the life of an Iteration. It will, however, alter
    * if the effort value of a story in the iteration is altered or a Story is removed or added.
    *
    * @return int representing the current velocity (number of Story Points) in whatever measure
    *         has been set in the parent project.
    */
   public int getStoryPoints() {
      int velocity = 0;
      for (BacklogItem item : backlog) {
         velocity += item.getEffort();
      }
      return velocity;
   }

   public int getStoryPointsCompleted() {
      int total = 0;
      for(BacklogItem item : backlog) {
         if(item.getStatus() == Status.DONE) {
            total += item.getEffort();
         }
      }
      return total;
   }

   public BacklogItem findBacklogItemByReference(String ref) {
      for (BacklogItem item : backlog) {
         if (item.getReference().equals(ref)) {
            return item;
         }
         BacklogItem task = item.findTaskByReference(ref);
         if(task != null) {
            return task;
         }
      }
      return null;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this)
         return true;
      if (this.id == null || !(obj instanceof Iteration))
         return false;
      Iteration iteration = (Iteration) obj;
      return this.id.equals(iteration.getId());
   }

   @Override
   public int hashCode() {
      return this.id == null ? super.hashCode() : this.id.hashCode();
   }

   @Override
   public String toString() {
      StringBuilder output = new StringBuilder();
      output.append(Iteration.class.getName());
      output.append("[");
      output.append("id=").append(id).append(",");
      output.append("version=").append(version).append(",");
      output.append("title=").append(title).append(",");
      output.append("description=").append(description).append(",");
      output.append("startDate=").append(startDate).append(",");
      output.append("endDate=").append(endDate).append(",");
      output.append("project=").append(project).append("]");
      return output.toString();
   }

   public String getBacklogAsCSV() {
      StringBuilder result = new StringBuilder();
      for(BacklogItem item : backlog) {
         result.append(item.toCSV());
         result.append("\n");
      }
      return result.toString();
   }

}
