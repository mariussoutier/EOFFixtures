package de.soutier.fixtures;

import er.extensions.ERXFrameworkPrincipal;
import er.extensions.foundation.ERXProperties;

public class FixturesFrameworkPrincipal extends ERXFrameworkPrincipal {
	static {
		setUpFrameworkPrincipalClass(FixturesFrameworkPrincipal.class);
	}

	@Override
	public void finishInitialization() {
	}

	@Override
	public void didFinishInitialization() {
		super.didFinishInitialization();
		if (ERXProperties.booleanForKeyWithDefault("EOFFixtures.loadInitialData", false))
			Fixtures.load();
	}
}
