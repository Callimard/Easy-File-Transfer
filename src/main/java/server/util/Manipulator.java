package server.util;

import server.util.exception.ManipulatorException;

public abstract class Manipulator {

	// Constants.

	// Variables.

	/**
	 * <p>
	 * Say if the creator has already create or not.
	 * </p>
	 * <p>
	 * Inherited class can manipulated but you must be careful when you use it to
	 * don't break the consistency of the manipulator.
	 * </p>
	 */
	protected boolean isAlreadyCreate = false;

	// Constructors.

	/**
	 * <p>
	 * Initialize the boolean isAlreadyCreate at true or false.
	 * </p>
	 * <p>
	 * If you put true, it means that the group of objects manipulated by the
	 * manipulator is in the state it should have been if it had been created by the
	 * manipulator.
	 * </p>
	 * <p>
	 * <b>If it is not the case, the manipulator risk to throw
	 * {@link ManipulatorException} when you will call methods update and
	 * delete.</b>
	 * </p>
	 * 
	 * <p>
	 * If you put true, you can't use create without done a delete before.
	 * </p>
	 * 
	 * @param isAlreadyCreate
	 */
	protected Manipulator(boolean isAlreadyCreate) {
		this.isAlreadyCreate = isAlreadyCreate;
	}

	/**
	 * <p>
	 * Initialize the boolean isAlreadyCreate at false.
	 * </p>
	 */
	protected Manipulator() {
		this(false);
	}

	// Methods.

	/**
	 * <p>
	 * The treatment which will be done when {@link Manipulator#create()} will be
	 * call.
	 * </p>
	 * 
	 * <p>
	 * <strong>/!\Normally you must not use the
	 * {@link Manipulator#isAlreadyCreate}/!\</strong>
	 * </p>
	 * 
	 * @throws ManipulatorException
	 */
	protected abstract void createProtected() throws ManipulatorException;

	/**
	 * <p>
	 * The treatment which will be done when {@link Manipulator#update()} will be
	 * call.
	 * </p>
	 * 
	 * <p>
	 * <strong>/!\Normally you must not use the
	 * {@link Manipulator#isAlreadyCreate}/!\</strong>
	 * </p>
	 * 
	 * @throws ManipulatorException
	 */
	protected abstract void updateProtected() throws ManipulatorException;

	/**
	 * <p>
	 * The treatment which will be done when {@link Manipulator#delete()} will be
	 * call.
	 * </p>
	 * 
	 * <p>
	 * <strong>/!\Normally you must not use the
	 * {@link Manipulator#isAlreadyCreate}/!\</strong>
	 * </p>
	 * 
	 * @throws ManipulatorException
	 */
	protected abstract void deleteProtected() throws ManipulatorException;

	/**
	 * <p>
	 * <strong>Can be call only one time.</strong>
	 * </p>
	 * <p>
	 * Create object(s) which is/are manipulated.
	 * </p>
	 * <p>
	 * You can't call the method create several times. To recall the create method.
	 * You must call before the delete method, else a {@link ManipulatorException}
	 * will be throw.
	 * </p>
	 * 
	 * @throws ManipulatorException
	 */
	public final void create() throws ManipulatorException {
		if (!this.isAlreadyCreate) {
			this.createProtected();
			this.isAlreadyCreate = true;
		} else {
			throw new ManipulatorException("The manipulator was already created. CREATE imposible.");
		}
	}

	/**
	 * <p>
	 * Update object(s) which is/are manipulated.
	 * </p>
	 * <p>
	 * You can update a manipulator only if it was create, else throw a
	 * {@link ManipulatorException}.
	 * </p>
	 * <p>
	 * After a creation, you can update it as many times as you want, but after a
	 * delete, the update is not possible (throw a {@link ManipulatorException}).
	 * </p>
	 * <p>
	 * After a delete, you must call create method before to re update the
	 * manipulator
	 * </p>
	 * 
	 * @throws ManipulatorException
	 */
	public final void update() throws ManipulatorException {
		if (this.isAlreadyCreate) {
			this.updateProtected();
		} else {
			throw new ManipulatorException("The manipulator was not created. UDPATE imposible.");
		}
	}

	/**
	 * <p>
	 * Delete object(s) which is/are manipulated.
	 * </p>
	 * <p>
	 * You can call the delete method if the method create was not call before, else
	 * throw a {@link ManipulatorException}
	 * </p>
	 * <p>
	 * The boolean isAlreadyCreate is make t false. After a delete you can call the
	 * method create.
	 * </p>
	 * 
	 * @throws ManipulatorException
	 */
	public final void delete() throws ManipulatorException {
		if (this.isAlreadyCreate) {
			this.deleteProtected();
			this.isAlreadyCreate = false;
		} else {
			throw new ManipulatorException("The manipulator was not created. DELETE imposible.");
		}
	}

	// Getters and Setters.

	public boolean isAlreadyCreate() {
		return isAlreadyCreate;
	}

}
