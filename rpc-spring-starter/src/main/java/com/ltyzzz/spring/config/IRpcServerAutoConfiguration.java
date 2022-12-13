package com.ltyzzz.spring.config;

import com.ltyzzz.core.event.IRpcListenerLoader;
import com.ltyzzz.core.server.ApplicationShutdownHook;
import com.ltyzzz.core.server.Server;
import com.ltyzzz.core.server.ServiceWrapper;
import com.ltyzzz.spring.common.IRpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Map;

public class IRpcServerAutoConfiguration implements InitializingBean, ApplicationContextAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(IRpcServerAutoConfiguration.class);

    private ApplicationContext applicationContext;

    @Override
    public void afterPropertiesSet() throws Exception {
        Map<String, Object> beanMap = applicationContext.getBeansWithAnnotation(IRpcService.class);
        if (beanMap.size() == 0) {
            return;
        }
        printBanner();
        long begin = System.currentTimeMillis();
        Server server = new Server();
        server.initServerConfig();
        IRpcListenerLoader iRpcListenerLoader = new IRpcListenerLoader();
        iRpcListenerLoader.init();
        for (String beanName : beanMap.keySet()) {
            Object bean = beanMap.get(beanName);
            IRpcService iRpcService = bean.getClass().getAnnotation(IRpcService.class);
            ServiceWrapper dataServiceWrapper = new ServiceWrapper(bean, iRpcService.group());
            dataServiceWrapper.setServiceToken(iRpcService.serviceToken());
            dataServiceWrapper.setLimit(iRpcService.limit());
            server.exportService(dataServiceWrapper);
            LOGGER.info(">>>>>>>>>>>>>>> [irpc] {} export success! >>>>>>>>>>>>>>> ",beanName);
        }
        long end = System.currentTimeMillis();
        ApplicationShutdownHook.registryShutdownHook();
        server.startApplication();
        LOGGER.info(" ================== [{}] started success in {}s ================== ",server.getServerConfig().getApplicationName(),((double)end-(double)begin)/1000);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    private void printBanner(){
        System.out.println();
        System.out.println("==============================================");
        System.out.println("|||---------- IRpc Starting Now! ----------|||");
        System.out.println("==============================================");
        System.out.println();
    }
}