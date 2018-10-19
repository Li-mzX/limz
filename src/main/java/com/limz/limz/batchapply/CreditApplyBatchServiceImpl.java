package javabean.newcredit.creditreport.service.serviceimpl;

import java.util.Date;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import com.j2ee.rbac.DAO.UserBasicInfo;
import com.j2ee.rbac.administer.UserAdmin;
import com.j2ee.rbac.helper.RbacException;
import javabean.newcredit.creditreport.dao.TCreditBatchApplyDao;
import javabean.newcredit.creditreport.po.TCreditBatchApply;
import javabean.newcredit.creditreport.po.TCreditBatchApplyDetail;
import javabean.newcredit.creditreport.service.CreditApplyBatchService;
import javabean.newcredit.creditreport.util.CreditApplyCallable;
import javabean.newcredit.creditreport.util.CreditApplyParameter;
import javabean.newcredit.creditreport.util.CreditApplyThread;
import javabean.newcredit.creditreport.util.CreditBatchApplyCodeBuilder;
import javabean.newcredit.creditreport.util.CreditBatchApplyException;
import javabean.newcredit.creditreport.util.StrTools;

/**
 * @ClassName: CreditApplyBatchServiceImpl
 * @Description: 批量资信申请实现类
 * @author Limz
 * @date 2017-10-16 上午11:13:36
 */
public class CreditApplyBatchServiceImpl implements CreditApplyBatchService {

    private TCreditBatchApplyDao tCreditBatchApplyDao;
    
    private UserAdmin useradmin;
    
    @Override
    public TCreditBatchApply batchCreditApply(String batchNo) {

        int time1 = (int) (System.currentTimeMillis()/1000);
        
        TCreditBatchApply batchApply = this.tCreditBatchApplyDao.getTCreditBatchApplyById(batchNo);
        if (batchApply == null) {
            throw new CreditBatchApplyException("【批量资信申请】无法获取批量申请信息");
        }
        if ("1".equals(batchApply.getState())) {
            throw new CreditBatchApplyException("【批量资信申请】该批次已被处理！");
        }
        UserBasicInfo userInfo = null;
        try {
            userInfo = useradmin.getUserInfoByBizUserId(batchApply.getUserId());
        } catch (RbacException e) {
            throw new CreditBatchApplyException("【批量资信申请】无法获取用户信息");
        }
        try {
            
            final UserBasicInfo ubi = userInfo;
            Set<TCreditBatchApplyDetail> set = batchApply.getCreditBatchApplyDetails();
            final BlockingQueue<TCreditBatchApplyDetail> queue = new LinkedBlockingQueue<TCreditBatchApplyDetail>(set);
            CreditBatchApplyCodeBuilder.initApply();
            int availableProcessors = 25;
            ExecutorService executor = Executors.newFixedThreadPool(availableProcessors);
            
            for (int i = 0; i < availableProcessors; i++) {
                CreditApplyCallable callBack = new CreditApplyCallable() {
                    @Override
                    public CreditApplyParameter next() {
                        TCreditBatchApplyDetail detail = null;
                        try {
                            if (queue.isEmpty()) {
                                return null;
                            }
                            detail = queue.poll();
                            CreditBatchApplyCodeBuilder codeBuild = CreditBatchApplyCodeBuilder.getInstance();
                            String countryCode = StrTools.BasesBuyernoGetCountyCode(detail.getBuyerNo());
                            CreditApplyParameter param;
                            synchronized (codeBuild) {
                                try {
                                    param = codeBuild.runGetParams(countryCode);
                                } catch (Exception e) {
                                    if (! (e instanceof CreditBatchApplyException)) {
                                        param = codeBuild.runGetParams(countryCode);
                                    }else {
                                        throw e;
                                    }
                                }
                            }
                            param.setDetail(detail);
                            param.setUserInfo(ubi);
                            return param;
                        } catch (Exception e) {
                            detail.setFaild();
                            if (e instanceof CreditBatchApplyException) {
                                detail.setErrorLog(e.getMessage());
                            }else {
                                detail.setErrorLog("申请异常，请重试或联系客服");
                            }
                            detail.setProcessTime(new Date(System.currentTimeMillis()));
                            return next();
                        }
                    }

                    @Override
                    public void callBack(TCreditBatchApplyDetail detail) {
                        queue.offer(detail);
                    }
                };
                CreditApplyThread applyThread = new CreditApplyThread(callBack);
                executor.execute(applyThread);
            }

            executor.shutdown();
            while (true) {
                if (executor.isTerminated()) {
                    break;
                }
                Thread.sleep(1000);
            }
            int time2 = (int) (System.currentTimeMillis()/1000);
            batchApply.setTakeTime(time2-time1);
            batchApply.setState("1");
            this.tCreditBatchApplyDao.saveOrUpdateTCreditBatchApply(batchApply);
        } catch (Exception e) {
            throw new CreditBatchApplyException("【批量资信申请】接口异常：" + e.getMessage());
        }
        return batchApply;
    }

    /**
     * @param tCreditBatchApplyDao the tCreditBatchApplyDao to set
     */
    public void settCreditBatchApplyDao(TCreditBatchApplyDao tCreditBatchApplyDao) {
        this.tCreditBatchApplyDao = tCreditBatchApplyDao;
    }

    /**
     * @param useradmin the useradmin to set
     */
    public void setUseradmin(UserAdmin useradmin) {
        this.useradmin = useradmin;
    }
    
}
