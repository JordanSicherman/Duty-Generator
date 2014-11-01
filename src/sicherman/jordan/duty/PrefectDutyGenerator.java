/**
 * 
 */
package sicherman.jordan.duty;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Jordan
 * 
 */
public class PrefectDutyGenerator extends Generator {

	public PrefectDutyGenerator(int weeks) {
		run(weeks);
	}

	@Override
	public void run(int weeks_to_generate) {
		int week = 0;

		while (week < weeks_to_generate) {
			int assigned = 0;
			Map<Student, Integer> students = new HashMap<Student, Integer>();

			// Iterate through all the students.
			while (assigned < 7)
				for (Student student : Main.main.getStudents())
					if (Job.PREFECT.assignTo(student, week, assigned)) {
						// Assign a student prefect duty.
						students.put(student, assigned);

						// Put priority on roommates.
						for (Student roommate : student.getRoomates())
							if (Job.PREFECT.assignTo(roommate, week, assigned))
								students.put(roommate, assigned);

						// Once the job is full, log history and rack it up for
						// a
						// new weekday.
						if (Job.PREFECT.isFull()) {
							for (Student s : Job.PREFECT.getWorkers()) {
								s.setJobHistory(student.getHouseJob(week - 1), week - 1);
								s.setJobHistory(student.getHouseJob(), week);
								s.setHouseJob(null);
							}
							Job.PREFECT.clearWorkers();
							assigned++;

							// Finish generating the week.
							if (assigned >= 7)
								break;
						}
					}

			savePrefectWeek(week, students);
			week++;

			// Shuffle so we can vary next week.
			Main.main.shuffleStudents();
		}
	}
}
