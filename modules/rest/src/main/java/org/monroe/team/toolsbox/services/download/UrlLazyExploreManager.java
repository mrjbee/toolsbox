package org.monroe.team.toolsbox.services.download;


import com.google.common.collect.Sets;
import org.monroe.team.toolsbox.us.ExploreDownloadUrlDefinition;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Named
public class UrlLazyExploreManager {

    private  static long executionIDGenerator = 0;

    private final Map<String, LazyExecution> lazyExecutionMap = new HashMap<String, LazyExecution>();

    public LazyExecution getLazyExecution(String pluginName, String executionId) {
        return lazyExecutionMap.get(pluginName+":"+executionId);
    }

    public synchronized String registerExecution(String pluginName, LazyExecution execution){
        long executionId = executionIDGenerator++;
        lazyExecutionMap.put(pluginName+":"+executionId,execution);
        execution.executionUrl = "plugin:"+pluginName+"/"+executionId;
        return execution.executionUrl;
    }

    public synchronized void cleanUpInvalidExecutions(){
        Set<String> keys = Sets.newHashSet(lazyExecutionMap.keySet());
        for (String executionId : keys) {
            LazyExecution execution = lazyExecutionMap.get(executionId);
            synchronized (execution){
                if (execution.isValid()){
                    lazyExecutionMap.remove(executionId).destroy();
                }
            }
        }
    }

    public static abstract class LazyExecution{

        private long dataIdGenerator = 0;
        private final Map<String,Object> stringObjectMap = new HashMap<String, Object>();
        private final long validTillDate;
        String executionUrl;

        protected LazyExecution(long validMs) {
            this.validTillDate = System.currentTimeMillis() + validMs;
        }

        final boolean isValid(){
         return  System.currentTimeMillis() < validTillDate;
        }

        final public String generateDataUrl(String dataId){
            return executionUrl +"/"+dataId;
        }

        final synchronized public String putData(Object object){
            Long id = dataIdGenerator++;
            stringObjectMap.put(id.toString(),object);
            return id.toString();
        }

        final synchronized protected Object getData(String dataId){
            return stringObjectMap.get(dataId);
        }

        public abstract ExploreDownloadUrlDefinition.ExploreDownloadUrlResponse execute(String dataId) throws ExploreDownloadUrlDefinition.UnreachableUrlException;

        public abstract void destroy();
    }
}
