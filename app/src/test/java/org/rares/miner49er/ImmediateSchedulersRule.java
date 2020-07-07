package org.rares.miner49er;

import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class ImmediateSchedulersRule implements TestRule {
    @Override
    public Statement apply(final Statement base, Description description) {
//        return new TrampolineReplacementStatement(base);
        return new IoReplacementStatement(base);
    }

    private class TrampolineReplacementStatement extends Statement {
        private Statement base;

        private TrampolineReplacementStatement(Statement base) {
            this.base = base;
        }

        @Override
        public void evaluate() throws Throwable {
            RxJavaPlugins.setIoSchedulerHandler(scheduler -> Schedulers.trampoline());
            RxJavaPlugins.setComputationSchedulerHandler(scheduler -> Schedulers.trampoline());
            RxJavaPlugins.setNewThreadSchedulerHandler(scheduler -> Schedulers.trampoline());

            try {
                base.evaluate();
            } finally {
                RxJavaPlugins.reset();
            }
        }
    }

    private class IoReplacementStatement extends Statement {
        private Statement base;

        private IoReplacementStatement(Statement base) {
            this.base = base;
        }

        @Override
        public void evaluate() throws Throwable {
//            RxJavaPlugins.setIoSchedulerHandler(scheduler -> Schedulers.io());
            RxJavaPlugins.setComputationSchedulerHandler(scheduler -> Schedulers.io());
            RxJavaPlugins.setNewThreadSchedulerHandler(scheduler -> Schedulers.io());

            try {
                base.evaluate();
            } finally {
                RxJavaPlugins.reset();
            }
        }
    }
}