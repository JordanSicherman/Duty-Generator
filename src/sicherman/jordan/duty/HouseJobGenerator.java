/**
 * 
 */
package sicherman.jordan.duty;

/**
 * @author Jordan
 * 
 */
public class HouseJobGenerator extends Generator {

	public HouseJobGenerator(int weeks) {
		run(weeks);
	}

	@Override
	public void run(int weeks_to_generate) {
		int week = 0;

		while (week < weeks_to_generate) {
			// Iterate through jobs and assign them to students.
			for (Job job : Main.main.getJobs()) {
				for (Student student : Main.main.getStudents()) {
					job.assignTo(student, week, -1);
					// Ensure roomates are placed on priority.
					for (Student roommate : student.getRoomates())
						job.assignTo(roommate, week, -1);
					if (job.isFull())
						break;
				}

				// Save.
				saveDutyWeek(week);
			}

			// Record a job history.
			for (Student student : Main.main.getStudents()) {
				student.setJobHistory(student.getHouseJob(week - 1), week - 1);
				student.setJobHistory(student.getHouseJob(), week);
				student.setHouseJob(null);
			}

			// Get ready to go again!
			for (Job job : Main.main.getJobs())
				job.clearWorkers();

			week++;

			// Add some spice to our lives.
			Main.main.shuffleStudents();
		}
	}
}
