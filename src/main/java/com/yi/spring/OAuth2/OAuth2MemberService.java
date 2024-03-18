package com.yi.spring.OAuth2;

import com.yi.spring.entity.User;
import com.yi.spring.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class OAuth2MemberService extends DefaultOAuth2UserService {
    @Autowired
    private OAuth2LoginUserRepository oAuthLoginUserRepository;
    @Autowired
    UserRepository userRepository;
    private Set<OAuth2LoginUser> reserveSaveUsers = new HashSet<>();

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User user2 = super.loadUser(userRequest);
        Map<String, Object> attributes = user2.getAttributes();
        String attrs = attributes.toString();
//        log.info("ATTR INFO : {}", attrs);

        String oauthType = userRequest.getClientRegistration().getRegistrationId();
        //oauth 타입에 따라 데이터
//        String email = switch (oauthType.toLowerCase()) {
//            case "kakao" -> ((Map<String, Object>) attributes.get("kakao_account")).get("email").toString();
//            case "google" -> attributes.get("email").toString();
//            case "naver" -> ((Map<String, Object>) attributes.get("response")).get("email").toString();
//            default -> null;
//        };
        Matcher matcher = Pattern.compile(
                "[{, ]email=([^,} ]+)"
        ).matcher( attrs );
        String email = matcher.find() ? matcher.group(1) : null;

        OAuth2LoginUser loginUser = getUserByEmailAndOAuthType(email, oauthType).orElseGet(
            ()->{
//            log.info("{}({}) NOT EXISTS. REGISTER", email, oauthType);
            OAuth2LoginUser user = new OAuth2LoginUser();
            user.setHashValue( user2.getName().hashCode() );
            user.setEmail( email );
            user.setOauthType( oauthType );
            user.setAttributes( attrs );
            save(user);
            return user;
        });

//        final Object[] retObj = {null};
//        userRepository.findByUserId( email ).ifPresentOrElse(
//                member-> retObj[0] = org.springframework.security.core.userdetails.User.builder()
//                    .username(member.getUserId())
//                    .password(member.getUserPassword())
//                    .roles(member.getUserAuth())
//                    .build(),
//                ()-> retObj[0] = user2
//        );
//        return (OAuth2User) retObj[0];

//        return org.springframework.security.core.userdetails.User.builder()
//                .username(o2User.getUserId())
////                .password(member.getUserPassword())
//                .roles(member.getUserAuth())
//                .build();




        return super.loadUser(userRequest);
    }

    public void save(OAuth2LoginUser user) {
        reserveSaveUsers.add( user );

////        OAuth2LoginUser result = o2userRepository.save(user);
//        OAuth2LoginUser test = new OAuth2LoginUser();
//        test.setHashValue( 1234 );
//        try {
//            OAuth2LoginUser result = oAuthLoginUserRepository.save(test);
//            System.out.println( result );
//        }catch ( Exception e ) {
//            System.out.println( "asfdsfsdfsdf ");
//            e.printStackTrace();
//        }
    }

    public void saveAll() {
        oAuthLoginUserRepository.saveAll(reserveSaveUsers);
        for ( OAuth2LoginUser member : reserveSaveUsers )
        {
            User user = new User();
            String[] keys = _getKeyNames( member.getOauthType() );
            List<Map.Entry<String, Consumer<String>>> listSetFunction = Arrays.asList(
                    new AbstractMap.SimpleEntry<>(keys[0], user::setUserName),
                    new AbstractMap.SimpleEntry<>(keys[1], user::setUserId),
                    new AbstractMap.SimpleEntry<>(keys[1], user::setUserEmail),
                    new AbstractMap.SimpleEntry<>(keys[2], user::setUserTel)
            );

            String attributes = member.getAttributes();
            for (Map.Entry<String, Consumer<String>> entry : listSetFunction) {
                Matcher matcher = Pattern.compile(
                        "[{, ]"+entry.getKey()+"=([^,} ]+)"
                ).matcher( attributes );
                if ( matcher.find() )
                    entry.getValue().accept( matcher.group(1));
            }
            user.setUserAuth( "USER" );
            user.setProvider( member.getOauthType() );
            user.setUserPassword( Base64.getEncoder().encodeToString( attributes.substring(0, 20).getBytes() ));
            userRepository.save( user );
        }

        reserveSaveUsers.clear();
    }

    private static final String[] KAKAO_KEY_NAMES = { "nickname", "email", "tel" };
    private static final String[] NAVER_KEY_NAMES = { "name", "email", "mobile" };
    private static final String[] GOOGLE_KEY_NAMES = { "name", "email", "tel" };
    private String[] _getKeyNames(String oauthType) {
        return switch (oauthType) {
            case "naver" -> NAVER_KEY_NAMES;
            case "google" -> GOOGLE_KEY_NAMES;
//            case "kakao" -> KAKAO_KEY_NAMES;
            default -> KAKAO_KEY_NAMES;
        };
    }

    public Optional<OAuth2LoginUser> getUserByEmailAndOAuthType(String email, String oauthType){
        return oAuthLoginUserRepository.findByEmailAndOauthType(email, oauthType);
    }

    public User findUser(Principal principal){
        String userId = "";
        String oProvider = null;
        if (principal instanceof OAuth2AuthenticationToken token)
        {
            String attrs = token.getPrincipal().getAttributes().toString();

            Matcher matcher = Pattern.compile(
                    "[{, ]email=([^,} ]+)"
            ).matcher( attrs );
            if ( matcher.find() )
                userId = matcher.group(1);

            oProvider = token.getAuthorizedClientRegistrationId();
//            userId = switch ( token.getAuthorizedClientRegistrationId() ) {
//                case "kakao" -> (String) ((Map)token.getPrincipal().getAttribute( "kakao_account" )).get( "email" );
//                case "naver" -> (String) ((Map)token.getPrincipal().getAttribute( "response" )).get( "email" );
//                case "google" -> token.getPrincipal().getAttribute("email");
//                default -> "";
//            };
        }
        else if ( null != principal )
        {
            userId = principal.getName();
        }

        return userRepository.findByUserIdAndProvider( userId, oProvider ).orElse( null );
    }



}
