package de.tuberlin.aot.thesis.slienau.scheduler.interfaces;

import de.tuberlin.aot.thesis.slienau.scheduler.strategy.AppDeployment;

import java.util.List;

public interface Scheduler {

    AppDeployment getOptimalDeployment();

    List<AppDeployment> getValidDeployments();
}
