package org.monroe.team.toolsbox.logging;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

final public class Logs {

    private final static Logger core = build("Core");

    final public static Logger forFeature(String featureName) {
        return build("feature."+featureName);
    }

    final private static Logger build(String postfix) {
        return LogManager.getLogger("toolsbox."+postfix);
    }
}