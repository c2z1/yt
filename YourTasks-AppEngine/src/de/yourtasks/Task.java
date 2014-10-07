package de.yourtasks;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Task {

	@Id
	private Long id;
	private String name;
	private Integer prio;
	private Date completed;
	private Long parentTaskId;
	private String description;
	private Integer repeatIntervalDays;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Integer getPrio() {
		return prio;
	}
	public void setPrio(Integer prio) {
		this.prio = prio;
	}
	public Date getCompleted() {
		return completed;
	}
	public void setCompleted(Date completed) {
		this.completed = completed;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Long getParentTaskId() {
		return parentTaskId;
	}
	public void setParentTaskId(Long parentTaskId) {
		this.parentTaskId = parentTaskId;
	}
	public Integer getRepeatIntervalDays() {
		return repeatIntervalDays;
	}
	public void setRepeatIntervalDays(Integer repeatIntervalDays) {
		this.repeatIntervalDays = repeatIntervalDays;
	}
	
}
