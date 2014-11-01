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
public class Job {

	public static Job PREFECT;
	private final String name;
	private final int capacity;
	private final List<Student> workers = new ArrayList<Student>();
	private final List<Integer> gradesFor;

	public Job(String name, int capacity, List<Integer> gradesFor) {
		this.name = name;
		this.capacity = capacity;
		this.gradesFor = gradesFor;
	}

	public String getName() {
		return name;
	}

	public int getCapacity() {
		return capacity;
	}

	public List<Student> getWorkers() {
		return workers;
	}

	public void clearWorkers() {
		workers.clear();
	}

	public boolean isFull() {
		return getWorkers().size() >= getCapacity();
	}

	/**
	 * A no-nonsense, simple way to map a number to a weekday.
	 * 
	 * @param num
	 *            A number (0->6)
	 * @return A weekday (based on the entry found at Generator.weekdays).
	 */
	private String numToDay(int num) {
		return num >= 0 && num <= 6 ? Generator.weekdays[num] : "Notday";
	}

	/**
	 * Assign a job to a student for a given day in a given week.
	 * 
	 * @param student
	 *            The student.
	 * @param current_week
	 *            The current week (offset from today).
	 * @param current_day
	 *            The day (0->6) or -1 if generating for a job other than
	 *            prefect duty.
	 * @return True if we could assign the job, false otherwise.
	 */
	public boolean assignTo(Student student, int current_week, int current_day) {
		/*
		 * Don't accept duty:
		 * * As a day student on a day-student-exempt day.
		 * * If exempt.
		 * * If not for given grade.
		 * * If full.
		 * * If has house job.
		 */
		if (student.isDayStudent() && Main.main.getData().getStringList("Settings.Day Students.Exemptions").contains(numToDay(current_day)))
			return false;
		if (student.getExemptions().contains(numToDay(current_day)) || !gradesFor.contains(student.getGrade()))
			return false;
		if (isFull() || student.getHouseJob() != null)
			return false;

		// Log the students we have to work in the valid grades for this job.
		int inGrade = 0;
		for (int i : gradesFor)
			inGrade += Main.main.studentsIn(i, false);

		// Special case if we're assigning prefect duty.
		if (this == Job.PREFECT) {
			boolean canAcceptJob = true;
			// Space at week offset intervals.
			for (int i = 1; i < Math.floor(inGrade / (7 * Job.PREFECT.getCapacity())) - 1; i++)
				if (student.getHouseJob(current_week - i) != null)
					canAcceptJob = false;
			if (canAcceptJob)
				student.setHouseJob(this);
			else
				return false;
			return workers.add(student);
		}

		// Remove day students from our valid workers (if day students aren't
		// workers)
		if (!Main.main.getData().getBoolean("Settings.Day Students.House Jobs"))
			for (int i : gradesFor)
				inGrade -= Main.main.studentsIn(i, true);

		/*
		 * Don't accept duty if we had a house job last week or in the valid history
		 * (based to cycle at greatest distance between jobs).
		 */
		if (student.getHouseJob(current_week - 1) != null)
			return false;
		for (int i = 1; i < Math.floor(inGrade / Main.main.totalJobSpots()) - 1; i++)
			if (student.getHouseJob(current_week - i) != null && student.getHouseJob(current_week - i).getName().equals(getName()))
				return false;

		student.setHouseJob(this);

		return workers.add(student);
	}
}
