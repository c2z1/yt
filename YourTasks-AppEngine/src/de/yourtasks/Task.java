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
	private Long projectId;
	
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
	public Long getProjectId() {
		return projectId;
	}
	public void setProjectId(Long projectId) {
		this.projectId = projectId;
	}
}
