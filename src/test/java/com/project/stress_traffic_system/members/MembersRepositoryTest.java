/*
package com.project.stress_traffic_system.members;

import com.project.stress_traffic_system.members.entity.Members;
import com.project.stress_traffic_system.members.entity.MembersRoleEnum;
import com.project.stress_traffic_system.members.repository.MembersRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

//@DataJpaTest //h2 DB가 default
@SpringBootTest
public class MembersRepositoryTest {

    @Autowired //회원을 조회할 때는 실제 DB 사용
    private MembersRepository membersRepository;

    @Mock //테스트에 이용할 가짜 객체
    private MembersRepository MockmembersRepository;

    @Mock(name = "USERS")
    private Members MockMember;

    private Members savedMockMember;

    //Mock 엔티티를 사용하기 때문에 실제DB에 저장되지 않는다.
    @DisplayName("멤버가 DB에 저장이 잘 되는지 확인")
    @Test
    public void testSave() {
        //given
        String username = "usertest";
        String password = "abcde123?";
        String address = "서울";
        MembersRoleEnum role = MembersRoleEnum.MEMBER;

        //가짜 멤버
        MockMember = new Members(username, password, address, role);
        //custom한 리포지터리 ->MockmembersRepository에 멤버를 저장하면 MockMember를 반환
        when(MockmembersRepository.save(any(Members.class))).thenReturn(MockMember);

        // when
        Members savedMember = MockmembersRepository.save(MockMember);

        // then
        assertThat(savedMember.getUsername()).isEqualTo(MockMember.getUsername());
        assertThat(savedMember.getPassword()).isEqualTo(MockMember.getPassword());
        assertThat(savedMember.getRole()).isEqualTo(MockMember.getRole());
    }

    @Test
    @DisplayName("저장된 멤버가 제대로 조회되는지 확인")
    void findMember() {
        //given
        //2000073 Id의 회원을 조회
        Long findId = 2000072L;
        //2000073 Id의 정보
        String username = "usertest";
        String password = "abcde123?";
        String address = "서울";
        MembersRoleEnum role = MembersRoleEnum.MEMBER;

        savedMockMember = new Members(username, password, address, role);

        // when
        Members findMember = membersRepository.findById(findId)
                .orElseThrow(() -> new IllegalArgumentException("Wrong MemberId:<" + savedMockMember.getId() + ">"));

        // then
        assertThat(findMember.getUsername()).isEqualTo(savedMockMember.getUsername());
        assertThat(findMember.getPassword()).isEqualTo(savedMockMember.getPassword());
        assertThat(findMember.getRole()).isEqualTo(savedMockMember.getRole());
    }
}*/
