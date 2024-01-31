package UMC.campusNote.auth.service;

import UMC.campusNote.auth.dto.JoinReqDto;
import UMC.campusNote.auth.dto.JoinResDto;
import UMC.campusNote.auth.dto.LoginReqDto;
import UMC.campusNote.auth.dto.LoginResDto;
import UMC.campusNote.auth.jwt.JwtProvider;
import UMC.campusNote.auth.redis.RedisProvider;
import UMC.campusNote.common.exception.GeneralException;
import UMC.campusNote.user.entity.User;
import UMC.campusNote.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static UMC.campusNote.common.code.status.ErrorStatus.USER_NOT_FOUND;


@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final RedisProvider redisProvider;

    @Override
    @Transactional
    public JoinResDto join(JoinReqDto joinReqDto) {
        User user = joinReqDto.toEntity();
        User savedUser = userRepository.save(user);
        String accessToken = jwtProvider.generateToken(user);
        String refreshToken = jwtProvider.generateRefreshToken(user);
        saveUserToken(savedUser, refreshToken);
        return JoinResDto.fromEntity(accessToken, refreshToken);
    }

    @Override
    @Transactional
    public LoginResDto login(LoginReqDto loginReqDto) {
        User user = userRepository.findByClientId(loginReqDto.getClientId())
                .orElseThrow(() -> new GeneralException(USER_NOT_FOUND));
        String accessToken = jwtProvider.generateToken(user);
        String refreshToken = jwtProvider.generateRefreshToken(user);
        revokeAllUserTokens(user);
        saveUserToken(user, refreshToken);
        return LoginResDto.fromEntity(accessToken, refreshToken);
    }

    private void saveUserToken(User user, String refreshToken) {
        //key는 사용자 이메일과 토큰 발급 시간으로 구성 // 추후에 발급 시간이 아닌 기기로 구분하는 거로 수정해야함
        //redisService.setValueOps(user.getEmail() + ":" + issuedAt, refreshToken);
        redisProvider.setValueOps(user.getClientId(), refreshToken);
        redisProvider.expireValues(user.getClientId());
    }


    private void revokeAllUserTokens(User user) {
        redisProvider.deleteValueOps(user.getClientId());
    }


}