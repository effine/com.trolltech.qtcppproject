package com.trolltech.qtcppproject.qmake;

import java.util.Properties;
import org.eclipse.core.resources.IProject;

public interface IQMakeEnvironmentModifier
{

    public abstract Properties getModifiedEnvironment(IProject iproject, Properties properties);
}
