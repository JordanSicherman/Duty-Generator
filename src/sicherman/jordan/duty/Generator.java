/**
 * 
 */
package sicherman.jordan.duty;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Jordan
 * 
 */
public abstract class Generator {

	// Formats for generation.
	private static final SimpleDateFormat fileFormat = new SimpleDateFormat("MMM d");
	private static final SimpleDateFormat prefectFormat = new SimpleDateFormat("EEE, MMM d");

	// Acceptable weekday formats.
	public static final String[] weekdays = new String[] { "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday" };

	/**
	 * Run the generator for a given number of weeks.
	 * 
	 * @param weeks_to_generate
	 *            The given number of weeks.
	 */
	public abstract void run(int weeks_to_generate);

	/**
	 * Save a duty week at a given week.
	 * 
	 * @param week
	 *            The week offset (from today)
	 */
	public void saveDutyWeek(int week) {
		// Initialize calendars for measuring.
		Calendar from = Calendar.getInstance();
		from.add(Calendar.WEEK_OF_YEAR, week);
		from.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		Calendar to = (Calendar) from.clone();
		to.add(Calendar.WEEK_OF_YEAR, 1);
		to.add(Calendar.DAY_OF_YEAR, -1);

		BufferedWriter writer = null;
		try {
			// Make files and folders.
			File folder = new File("Duty Schedule");
			folder.mkdir();
			File logFile = new File("Duty Schedule" + File.separator + fileFormat.format(from.getTime()) + " - "
					+ fileFormat.format(to.getTime()) + ".txt");

			writer = new BufferedWriter(new FileWriter(logFile));

			// Write.
			for (Job job : Main.main.getJobs()) {
				writer.write(job.getName() + ": ");
				StringBuffer buffer = new StringBuffer();
				for (Student student : job.getWorkers()) {
					writeToSpecificFile(student,
							job.getName()
									+ (job.getWorkers().size() > 1 ? " with " + listToStringExcluding(job.getWorkers(), student) : ""),
							from);
					buffer.append(student.getName());
					buffer.append(", ");
				}
				String str;
				if (buffer.indexOf(",") != -1)
					str = buffer.substring(0, buffer.lastIndexOf(", "));
				else
					str = buffer.toString();
				writer.write(str);
				writer.write("\n");
			}
		} catch (Exception exc) {
			exc.printStackTrace();
		} finally {
			try {
				writer.close();
			} catch (Exception exc) {
				exc.printStackTrace();
			}
		}
	}

	/**
	 * A simple method to turn a list of students into a list of names,
	 * excluding one student.
	 * 
	 * @param students
	 *            The list of students.
	 * @param excluding
	 *            The student to exclude from the list.
	 * @return The students in the list (without the one excluded) as a
	 *         comma-delimited list.
	 */
	private String listToStringExcluding(List<Student> students, Student excluding) {
		String returned = "";
		for (Student student : students)
			if (excluding == null || !student.getName().equals(excluding.getName()))
				returned += student.getName() + ", ";
		return returned.endsWith(", ") ? returned.substring(0, returned.length() - 2) : returned;
	}

	/**
	 * Compose a list of objects in a map with a given value.
	 * 
	 * @param map
	 *            The map.
	 * @param value
	 *            The value.
	 * @return The list of objects with the given value in the given map.
	 */
	private <T, E> List<T> fromMapWithValue(Map<T, E> map, E value) {
		List<T> list = new ArrayList<T>();
		for (T key : map.keySet())
			if (map.get(key).equals(value))
				list.add(key);

		return list;
	}

	/**
	 * Save a prefect duty week at a given week.
	 * 
	 * @param week
	 *            The week.
	 * @param prefects
	 *            The prefects on duty, mapped to their weekday (0 indicates
	 *            Monday, 6 indicates Sunday)
	 */
	public void savePrefectWeek(int week, Map<Student, Integer> prefects) {
		// Initialize Calendars for measuring.
		Calendar from = Calendar.getInstance();
		from.add(Calendar.WEEK_OF_YEAR, week);
		from.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		Calendar other = (Calendar) from.clone();
		int dOy = other.get(Calendar.DAY_OF_YEAR);
		Calendar to = (Calendar) from.clone();
		to.add(Calendar.WEEK_OF_YEAR, 1);
		to.add(Calendar.DAY_OF_YEAR, -1);

		BufferedWriter writer = null;
		try {
			// Make files and folders.
			File folder = new File("Prefect Schedule");
			folder.mkdir();
			File logFile = new File("Prefect Schedule" + File.separator + fileFormat.format(from.getTime()) + " - "
					+ fileFormat.format(to.getTime()) + ".txt");

			writer = new BufferedWriter(new FileWriter(logFile));

			Map<Integer, String> entries = new HashMap<Integer, String>();

			// Store important data and write to the specific files.
			for (Student student : prefects.keySet()) {
				List<Student> onJob = fromMapWithValue(prefects, prefects.get(student));
				writeToSpecificFile(student, prefectFormat.format(other.getTime()) + ": Prefect Duty"
						+ (onJob.size() > 1 ? " with " + listToStringExcluding(onJob, student) : ""), from);
				other.set(Calendar.DAY_OF_YEAR, dOy + prefects.get(student));

				// Write for later.
				if (!entries.containsKey(prefects.get(student)))
					entries.put(prefects.get(student), listToStringExcluding(onJob, null));
			}

			// Write in order.
			int desired = 0;
			while (desired <= 6)
				for (int key : entries.keySet())
					if (key == desired) {
						writer.write(weekdays[key] + ": " + entries.get(key));
						writer.write("\n");
						desired++;
					}
		} catch (Exception exc) {
			exc.printStackTrace();
		} finally {
			try {
				writer.close();
			} catch (Exception exc) {
				exc.printStackTrace();
			}
		}
	}

	/**
	 * A somewhat taxing way to write every student into a specific file for
	 * ease of checking personal duty schedules.
	 * 
	 * @param student
	 *            The student.
	 * @param entry
	 *            The line to write to file.
	 * @param calendar
	 *            The calendar that will provide a file name.
	 */
	private void writeToSpecificFile(Student student, String entry, Calendar calendar) {
		BufferedWriter writer = null;
		try {
			// Make files and folders.
			File folder = new File("Student Specific" + File.separator + "Grade " + student.getGrade() + File.separator + student.getName());
			folder.mkdirs();
			File logFile = new File("Student Specific" + File.separator + "Grade " + student.getGrade() + File.separator
					+ student.getName() + File.separator + fileFormat.format(calendar.getTime()) + ".txt");

			// Write.
			writer = new BufferedWriter(new FileWriter(logFile));
			writer.write(entry);
			writer.write("\n");
		} catch (Exception exc) {
			exc.printStackTrace();
		} finally {
			try {
				writer.close();
			} catch (Exception exc) {
				exc.printStackTrace();
			}
		}
	}
}
