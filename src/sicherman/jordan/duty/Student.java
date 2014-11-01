/**
 * 
 */
package sicherman.jordan.duty;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Jordan
 * 
 */
public class Student {

	private final List<Student> roomates = new ArrayList<Student>();
	private final String name;
	private final int room, grade;
	private Job job;
	private final List<Job> jobHistory = new ArrayList<Job>();
	private final List<String> exemptions;
	private final boolean day;

	public Student(String name, int grade, int room, boolean day, List<String> exemptions) {
		this.room = room;
		this.grade = grade;
		this.name = name;
		this.day = day;
		this.exemptions = exemptions;
	}

	/**
	 * Load roommates (same room number)
	 */
	public void loadRoommates() {
		for (Student student : Main.main.getStudents())
			if (student.getRoom() == getRoom() && !student.getName().equals(getName()))
				roomates.add(student);
	}

	public int getGrade() {
		return grade;
	}

	public int getRoom() {
		return room;
	}

	public String getName() {
		return name;
	}

	public boolean isDayStudent() {
		return day;
	}

	public List<Student> getRoomates() {
		return roomates;
	}

	public List<String> getExemptions() {
		return exemptions;
	}

	public Job getHouseJob() {
		return job;
	}

	public Job getHouseJob(int week) {
		try {
			return jobHistory.get(week);
		} catch (Exception exc) {
			return null;
		}
	}

	/**
	 * Record the history of our work.
	 * 
	 * @param job
	 *            The job to log.
	 * @param week
	 *            The week (offset) to log under.
	 */
	public void setJobHistory(Job job, int week) {
		if (week < 0)
			return;
		jobHistory.add(week, job);
	}

	public void setHouseJob(Job job) {
		this.job = job;
	}
}
