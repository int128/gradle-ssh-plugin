package org.hidetake.gradle.ssh.internal

import org.hidetake.gradle.ssh.api.OperationEventListener
import org.hidetake.gradle.ssh.api.OperationHandler

/**
 * Null implementation of {@link OperationHandler} for dry-run.
 * 
 * @author hidetake.org
 *
 */
class DryRunOperationHandler implements OperationHandler {
	/**
	 * Event listeners.
	 */
	final List<OperationEventListener> listeners = []

	@Override
	void execute(String command) {
		listeners*.beginOperation('execute', command)
	}

	@Override
	void executeBackground(String command) {
		listeners*.beginOperation('executeBackground', command)
	}

	@Override
	void get(String remote, String local) {
		listeners*.beginOperation('get', remote, local)
	}

	@Override
	void put(String local, String remote) {
		listeners*.beginOperation('put', remote, local)
	}

	@Override
	void execute(Map options, String command) {
		listeners*.beginOperation('execute', options, command)
	}

	@Override
	void executeBackground(Map options, String command) {
		listeners*.beginOperation('executeBackground', options, command)
	}

	@Override
	void get(Map options, String remote, String local) {
		listeners*.beginOperation('get', options, remote, local)
	}

	@Override
	void put(Map options, String local, String remote) {
		listeners*.beginOperation('put', options, remote, local)
	}
}
