package com.batch.pt.persnertrainingbatch.job.pass;

import com.batch.pt.persnertrainingbatch.repository.pass.BulkPassEntity;
import com.batch.pt.persnertrainingbatch.repository.pass.BulkPassRepository;
import com.batch.pt.persnertrainingbatch.repository.pass.PassEntity;
import com.batch.pt.persnertrainingbatch.repository.pass.PassModelMapper;
import com.batch.pt.persnertrainingbatch.repository.pass.PassRepository;
import com.batch.pt.persnertrainingbatch.repository.user.UserGroupMappingEntity;
import com.batch.pt.persnertrainingbatch.repository.user.UserGroupMappingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.batch.pt.persnertrainingbatch.repository.pass.BulkPassStatus.COMPLETED;
import static com.batch.pt.persnertrainingbatch.repository.pass.BulkPassStatus.READY;

@Component
@RequiredArgsConstructor
public class AddPassesTasklet implements Tasklet {

    private final PassRepository passRepository;
    private final BulkPassRepository bulkPassRepository;
    private final UserGroupMappingRepository userGroupMappingRepository;


    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

        //이용권 시작 1일전 userGroup의 사용자들에게 이용권 추가
        final LocalDateTime startAt = LocalDateTime.now().minusDays(1);
        final List<BulkPassEntity> bulkPassEntities = bulkPassRepository.findByStatusAndStartedAtGreaterThan(READY, startAt);

        int count = 0;
        for (BulkPassEntity bulkPassEntity : bulkPassEntities) {
            //userGroup에 속한 userId 조회
            final List<String> userIds = userGroupMappingRepository.findByUserGroupId(bulkPassEntity.getUserGroupId())
                    .stream().map(UserGroupMappingEntity::getUserId).toList();

            //각 userId로 이용권 추가
            count += addPasses(bulkPassEntity, userIds);
            //pass 추가 이후 상태를 COMPLETED로 업데이트
            bulkPassEntity.setStatus(COMPLETED);
        }

        return RepeatStatus.FINISHED;
    }

    private int addPasses(BulkPassEntity bulkPassEntity, List<String> userIds) {
        List<PassEntity> passEntities = new ArrayList<>();
        for (String userId : userIds) {
            PassEntity passEntity = PassModelMapper.INSTANCE.toPassEntity(bulkPassEntity, userId);
            passEntities.add(passEntity);
        }
        return passRepository.saveAll(passEntities).size();
    }
}
