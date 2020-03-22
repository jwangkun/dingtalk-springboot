package com.softeng.dingtalk.service;

import com.softeng.dingtalk.entity.*;
import com.softeng.dingtalk.repository.AcRecordRepository;
import com.softeng.dingtalk.repository.BugDetailRepository;
import com.softeng.dingtalk.repository.BugRepository;
import com.softeng.dingtalk.repository.IterationDetailRepository;
import com.softeng.dingtalk.vo.BugCheckVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zhanyeye
 * @description
 * @create 3/8/2020 11:26 PM
 */
@Service
@Transactional
@Slf4j
public class BugService {
    @Autowired
    BugRepository bugRepository;
    @Autowired
    IterationDetailRepository iterationDetailRepository;
    @Autowired
    BugDetailRepository bugDetailRepository;
    @Autowired
    AcRecordRepository acRecordRepository;


    /**
     * 用户提交bug
     * @param bug
     */
    public void submitBug(Bug bug) {
        bugRepository.save(bug);
    }


    /**
     * 查询指定项目的bug
     * @param pid
     */
    public List<Bug> listProjectBug(int pid) {
        return bugRepository.findAllByProjectId(pid);
    }


    /**
     * 用户删除bug
     * @param id
     */
    public void rmbug(int id) {
        bugRepository.deleteById(id);
    }

    //todo 是否使用
    public List<Bug> listProjectBugByAuditor(int aid) {
        return bugRepository.listBugByAuditor(aid);
    }

//    public List<Bug> listBugByAuditorUncheck(int aid) {
//        return bugRepository.listAuditorUncheck(aid);
//    }
//    public List<Bug> listBugByAuditorcheck(int aid) {
//        return bugRepository.listAuditorUncheck(aid);
//    }


    // 审核人确认bug
    public void checkbug(@RequestBody BugCheckVO vo) {
        bugDetailRepository.deleteBugDetailByBugId(vo.getId());
        if (vo.isStatus() == false) { // bug 不存在
            bugRepository.updateBugStatus(vo.getId(), false);
        } else { // 存在bug
            Bug bug = bugRepository.findById(vo.getId()).get(); // 当前bug
            bug.setStatus(true);
            User auditor = bug.getProject().getAuditor(); // 审核人
            List<User> users = iterationDetailRepository.listUserByIterationId(vo.getIterationId()); // bug 所属迭代的所有用户
            List<BugDetail> bugDetails = new ArrayList<>();
            List<AcRecord> acRecords = new ArrayList<>();
            int cnt = users.size(); //迭代参与人数

            double ac;
            AcRecord acRecord;
            String reason;
            for (User u : users) {
                if (u.getId() != vo.getUid()) {
                    ac = - 0.1 / (cnt -1);
                    reason = "开发任务： " +  bug.getProject().getTitle() + " 存在bug, 非主要负责人";
                } else {
                    ac = - 0.1;
                    reason = "开发任务： " +  bug.getProject().getTitle() + " 存在bug, 为主要负责人";
                }
                acRecord = new AcRecord(u, auditor, ac, reason, AcRecord.BUG);
                acRecords.add(acRecord);
                BugDetail bugDetail = new BugDetail(new Bug(vo.getId()), u, false, ac, acRecord);
                bugDetails.add(bugDetail);
            }
            acRecordRepository.saveAll(acRecords);
            bugDetailRepository.saveAll(bugDetails);
        }
    }


}
