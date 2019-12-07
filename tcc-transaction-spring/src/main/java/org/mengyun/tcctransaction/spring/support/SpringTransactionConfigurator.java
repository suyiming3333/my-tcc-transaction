package org.mengyun.tcctransaction.spring.support;

import org.mengyun.tcctransaction.TransactionManager;
import org.mengyun.tcctransaction.TransactionRepository;
import org.mengyun.tcctransaction.recover.RecoverConfig;
import org.mengyun.tcctransaction.repository.CachableTransactionRepository;
import org.mengyun.tcctransaction.spring.recover.DefaultRecoverConfig;
import org.mengyun.tcctransaction.support.TransactionConfigurator;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by changmingxie on 11/11/15.
 *
 * todo sping 事务配置器
 */
public class SpringTransactionConfigurator implements TransactionConfigurator {

    private static volatile ExecutorService executorService = null;

    @Autowired
    private TransactionRepository transactionRepository;

    /**
     * 事务恢复配置
     * **/
    @Autowired(required = false)
    private RecoverConfig recoverConfig = DefaultRecoverConfig.INSTANCE;


    private TransactionManager transactionManager;

    public void init() {
        transactionManager = new TransactionManager();
        //todo 设置操作数据库的repository
        transactionManager.setTransactionRepository(transactionRepository);
        //todo 双重校验锁获取线程池
        if (executorService == null) {

            synchronized (SpringTransactionConfigurator.class) {

                if (executorService == null) {
//                    executorService = new ThreadPoolExecutor(recoverConfig.getAsyncTerminateThreadPoolSize(),
//                            recoverConfig.getAsyncTerminateThreadPoolSize(),
//                            0L, TimeUnit.SECONDS,
//                            new SynchronousQueue<Runnable>());
                    executorService = Executors.newCachedThreadPool();
                }
            }
        }

        transactionManager.setExecutorService(executorService);

        if (transactionRepository instanceof CachableTransactionRepository) {
            ((CachableTransactionRepository) transactionRepository).setExpireDuration(recoverConfig.getRecoverDuration());
        }
    }

    @Override
    public TransactionManager getTransactionManager() {
        return transactionManager;
    }

    @Override
    public TransactionRepository getTransactionRepository() {
        return transactionRepository;
    }

    @Override
    public RecoverConfig getRecoverConfig() {
        return recoverConfig;
    }
}