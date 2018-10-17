package server.sql.table_row;

import java.util.ArrayList;
import java.util.List;

public abstract class TableRow {

	// Constants.

	public final String tableName;

	public final int columnCount;

	public final int primaryKeyCount;

	protected final int[] tabColumnFlag;

	// Variables.

	/**
	 * A binary number which represents which column of the table is Complete or
	 * not.
	 */
	protected int completedColumnFlag = 0;

	// Constructors.

	protected TableRow(String tableName, int columnCount, int primaryKeyCount) {
		this.tableName = tableName;
		this.columnCount = columnCount;
		this.primaryKeyCount = primaryKeyCount;

		this.tabColumnFlag = this.initialiazeTabColumnFlag();
	}

	// Methods.

	/**
	 * Compare two table row with the primary key and only the primary key.
	 */
	@Override
	public abstract boolean equals(Object object);
	
	@Override
	public abstract TableRow clone();

	protected abstract int[] initialiazeTabColumnFlag();

	/**
	 * <p>
	 * Refine argument to avoid NullPointerException or SQLException or other
	 * Exception because one or more arguments are not correctly formated or don't
	 * have an appropriated value.
	 * </p>
	 * 
	 * <strong>/!\This method is call in the TableRow constructor./!\</strong>
	 * 
	 * <p>
	 * <strong>Example :</strong><br>
	 * If you have in your DB a column which can accept empty or null string. The
	 * attribute in the TableRow can be set at null or at an empty string. But when
	 * you want call the function clone() or copy this attribute, it is not a
	 * problem if the attribute is an empty string, but if it is null pointer, the
	 * command new String(attribute) will throw an <b>NullPointerException</b>
	 * because your attribute is null.<br>
	 * But if you put this command in refineArgument :
	 * <code>attribute = (attribute == null
	 * ? new String(""))</code> : attribute, your attribute will now always be an
	 * empty string. So now you are sure that when you clone or use a DAO, your
	 * guarantee that <b>NullPointerException</b> can't occurred and in your DAO you
	 * don't have to verify if the attribute is null or not.
	 * </p>
	 */
	protected abstract void refineArgument();

	/**
	 * Update the completed flag.
	 */
	protected abstract void updateCompletedColumnFlag();

	/**
	 * <strong>/!\Don't verify the DB state, so we can't know with this method if
	 * the row is already present or if there is/are some violations of constraints.
	 * /!\</strong>
	 * 
	 * @return true if the Table Row can be use by a DAO for create the row, else
	 *         false.
	 */
	public abstract boolean isReadyToBeCreated();

	/**
	 * <strong>/!\Don't verify the DB state, so we can't know with this method if
	 * the row is already present or if there is/are some violations of constraints.
	 * /!\</strong>
	 * 
	 * @return true if the Table Row can be use by a DAO for update the row, else
	 *         false.
	 */
	public abstract boolean isReadyToBeUpdated();

	/**
	 * <strong>/!\Don't verify the DB state, so we can't know with this method if
	 * the row is already present or if there is/are some violations of constraints.
	 * /!\</strong>
	 * 
	 * @return true if the Table Row can be use by a DAO for delete the row, else
	 *         false.
	 */
	public abstract boolean isReadyToBeDeleted();

	/**
	 * <p>
	 * <strong>This function is very important because it call all method which be
	 * make the table row coherent and usable by other object as the DAO.</strong>
	 * </p>
	 * <p>
	 * <b>it must be called at the end of each constructor of TableRow</b>
	 * </p>
	 */
	protected void reformAll() {
		this.refineArgument();
		this.updateCompletedColumnFlag();
	}

	/**
	 * <p>
	 * To verify the flag, we consider that Column Flag is a binary number with only
	 * one bit at 1 and this bit is at the index of the position of the column which
	 * it represents.
	 * </p>
	 * <p>
	 * <strong>Example :<br>
	 * if there is a table of 5 columns, each column flag will be respectively
	 * 0b0000_0001, 0b0000_0010, 0b0000_0100, 0b0000_1000, 0b0001_0000. </strong>
	 * </p>
	 * 
	 * @param columnFlag
	 *            - the flag which must be verify
	 * @return true if the flag represents a column flag, else false.
	 */
	protected boolean verifyColumnFlag(int columnFlag) {
		int i = 0;
		int nb_1 = 0;

		int flag = columnFlag;

		while (flag > 0) {
			if ((flag & 1) == 1)
				nb_1++;

			flag >>= 1;
			i++;
		}

		return nb_1 == 1 && i <= this.columnCount;
	}

	/**
	 * <p>
	 * Verify if the column represents by the flag is complete.
	 * </p>
	 * <p>
	 * A column is not complete when for example a column which must be not null is
	 * null or if a column has a empty string and it is not accepted.
	 * </p>
	 * 
	 * @param columnFlag
	 *            - the flag associate to the column.
	 * @return true if the column is completed, else false.
	 */
	public boolean columnIsCompleted(int columnFlag) {
		if (!this.verifyColumnFlag(columnFlag))
			throw new IllegalArgumentException("Flag non reconnu.");

		return (this.completedColumnFlag & columnFlag) == columnFlag;
	}

	/**
	 * 
	 * @return a list which contains all column flags which are not completed. If
	 *         there are no uncompleted column, return null.
	 */
	public List<Integer> getUnCompletedColumnFlags() {
		int flag = this.completedColumnFlag;

		int i = 0;

		ArrayList<Integer> listFlag = new ArrayList<>();

		while (i >= this.columnCount) {
			if ((flag & 1) == 0) {
				listFlag.add(this.tabColumnFlag[i]);
			}

			flag >>= 1;
			i++;
		}

		return listFlag.isEmpty() ? null : listFlag;
	}

	/**
	 * 
	 * @return a list which contains all column flags which are not completed. If
	 *         there are no uncompleted column, return null.
	 */
	public List<Integer> getCompletedColumnFlags() {
		int flag = this.completedColumnFlag;

		int i = 0;

		ArrayList<Integer> listFlag = new ArrayList<>();

		while (i >= this.columnCount && flag > 0) {
			if ((flag & 1) == 1) {
				listFlag.add(this.tabColumnFlag[i]);
			}

			flag >>= 1;
			i++;
		}

		return listFlag.isEmpty() ? null : listFlag;
	}

	// Getters and Setters.

	public int getColumnFlag() {
		return this.completedColumnFlag;
	}

}
