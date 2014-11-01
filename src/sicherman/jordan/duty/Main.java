/**
 * 
 */
package sicherman.jordan.duty;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import sicherman.jordan.duty.configuration.ConfigurationSection;
import sicherman.jordan.duty.configuration.file.YamlConfiguration;

/**
 * @author Jordan
 * 
 */
public class Main {

	private final List<Student> students = new ArrayList<Student>();
	private final List<Job> jobs = new ArrayList<Job>();
	private static Random random;
	public static Main main;
	private static YamlConfiguration data;

	public static void main(String[] args) {
		new Main();
	}

	public Main() {
		main = this;

		saveResource("data.yml", false);
		saveResource("Help.docx", false);

		data = YamlConfiguration.loadConfiguration(new File("data.yml"));

		if (getData().getInt("Settings.Generation.Seed") != -1)
			random = new Random(getData().getInt("Settings.Generation.Seed"));
		else
			random = new Random();

		delete(new File("Duty Schedule"));
		delete(new File("Prefect Schedule"));
		delete(new File("Student Specific"));

		loadData();
		generate();
	}

	/**
	 * Delete a directory.
	 * 
	 * @param directory
	 *            The directory to delete.
	 */
	private void delete(File directory) {
		if (directory.exists()) {
			File[] files = directory.listFiles();
			if (null != files) {
				for (int i = 0; i < files.length; i++) {
					if (files[i].isDirectory()) {
						delete(files[i]);
					} else {
						files[i].delete();
					}
				}
			}
		}
		// directory.delete();
	}

	/**
	 * Save an internal resource.
	 * 
	 * @param path
	 *            The path to the resource.
	 * @param overwrite
	 *            Whether or not to overwrite the file.
	 */
	private void saveResource(String path, boolean overwrite) {
		FileOutputStream out = null;
		try {
			InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
			File f = new File(path);
			if (f.exists() && !overwrite) { return; }
			out = new FileOutputStream(f);

			byte[] buffer = new byte[1024];
			int len = in.read(buffer);
			while (len != -1) {
				out.write(buffer, 0, len);
				len = in.read(buffer);
			}
		} catch (Exception exc) {
			exc.printStackTrace();
		} finally {
			try {
				if (out != null)
					out.close();
			} catch (Exception exc) {
				exc.printStackTrace();
			}
		}
	}

	public YamlConfiguration getData() {
		return data;
	}

	public List<Student> getStudents() {
		return students;
	}

	public void shuffleStudents() {
		Collections.shuffle(students, random);
	}

	public List<Job> getJobs() {
		return jobs;
	}

	/**
	 * Load the data from data.yml into memory.
	 */
	private void loadData() {
		ConfigurationSection section = getData().getConfigurationSection("Students");
		for (String key : section.getKeys(false))
			students.add(new Student(key, section.getInt(key + ".Grade"), section.getInt(key + ".Room Number"), section.getBoolean(key
					+ ".Day Student"), section.getStringList(key + ".Exemptions")));

		section = getData().getConfigurationSection("Jobs");
		for (String key : section.getKeys(false))
			if (!"Prefect".equals(key))
				jobs.add(new Job(key, section.getInt(key + ".Capacity"), section.getIntegerList(key + ".Grades")));
			else
				Job.PREFECT = new Job(key, section.getInt(key + ".Capacity"), section.getIntegerList(key + ".Grades"));

		for (Student student : getStudents())
			student.loadRoommates();
	}

	/**
	 * Count the students in a given grade.
	 * 
	 * @param grade
	 *            The grade to count.
	 * @param day_only
	 *            True if we should only count day students.
	 * @return The number of students counted, based on above parameters.
	 */
	public int studentsIn(int grade, boolean day_only) {
		int count = 0;
		for (Student student : getStudents())
			if (student.getGrade() == grade && (!day_only || student.isDayStudent()))
				count++;
		return count;
	}

	/**
	 * @return The total number of spaces available in non-prefect jobs.
	 */
	public int totalJobSpots() {
		int count = 0;
		for (Job job : getJobs())
			if (!job.equals(Job.PREFECT))
				count += job.getCapacity();
		return count;
	}

	/**
	 * Begin generating.
	 */
	private void generate() {
		shuffleStudents();
		int weeks = getData().getInt("Settings.Generation.Weeks");
		new HouseJobGenerator(weeks);
		new PrefectDutyGenerator(weeks);
	}
}
