package com.yi.spring.controller;

import com.yi.spring.OAuth2.OAuth2LoginUser;
import com.yi.spring.OAuth2.OAuth2LoginUserRepository;
import com.yi.spring.OAuth2.OAuth2MemberService;
import com.yi.spring.entity.Dinning;
import com.yi.spring.entity.meta.DinningReviewView;
import com.yi.spring.repository.*;
import com.yi.spring.service.DinningService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
@RequestMapping("/")
public class RestSearchController {

    @Autowired
    private DinningService dinningService;

    @Autowired
    private DinningRepository dinningRepository;
    @Autowired
    private DinningWithReviewRepository dinningWithReviewRepository;

//    @Autowired
//    private OAuth2LoginUserRepository o2o2o2o2o2o;
//
//    @Autowired
//    OAuth2MemberService testService;

    public RestSearchController(DinningService dinningService){
        this.dinningService = dinningService;
    }

//    @GetMapping("search")
    public String map(Model model) {
       List<Dinning> list = dinningRepository.findAll();
//
//        for (Dinning diningRest : list) {
//            System.out.println("Latitude: " + diningRest.getRestLatitude() + ", Longitude: " + diningRest.getRestLongitude() + "가게 이름" + diningRest.getRestName());
//        }

//        List<Dinning> respList = new ArrayList<>();
//        for (Dinning diningRest : list) {
//            Dinning elem = new Dinning();
//            elem.setRestLatitude( diningRest.getRestLatitude());
//            elem.setRestLongitude( diningRest.getRestLongitude());
//            elem.setRestName( diningRest.getRestName());
//            elem.setRestNo(diningRest.getRestNo());
//            respList.add(elem);
//        }

        System.out.println("Controller method called with keyword: " + "restName");



//        List<DinningDto> rest_name = dinningRepository.findByRestNameContainingFromView(restName);
//        List<Object[]> rest_name2 = dinningRepository.findByRestNameContainingFromView(restName);
//        List<DinningDto> rest_name = rest_name2.stream().map(DinningDto::new).toList();



//        List<Object[]> listOrderByRestScore2 = dinningRepository.getRestScore2();
//        List<Dinning> listOrderByRestScore = new ArrayList<>();
//        for ( Object[] items : listOrderByRestScore2 )
//        {
//            Dinning elem = (Dinning)items[0];
//            elem.setRestScore( (Double)items[1] );
//            listOrderByRestScore.add( elem );
//        }


        model.addAttribute("list", list);

//        List<Dinning> listOrderByRestScore = dinningRepository.getRestScore();
//        model.addAttribute("listOrderByRestScore", listOrderByRestScore);

        return "search";
    }

//    @GetMapping("/find_rest_name_x")
//    public ResponseEntity<List<Dinning>> findRestName_x(@RequestParam(name = "keyword") String restName, Model model) {
//        List<Dinning> rest_name = dinningRepository.findByRestNameContaining(restName);
//        System.out.println( rest_name );
//        return new ResponseEntity<>(rest_name, HttpStatus.OK);
//    }


    public static List<DinningReviewView> searchMain( String restName, Map<String, String> params, int pageSize
            , DinningWithReviewRepository inRepository )
    {
        List<DinningReviewView> dinningReviewList = new ArrayList<>();
        String filter1 = params.get( "filter1" );
        String searchCategory = params.get( "filter2" );
        if ( null != searchCategory )
            searchCategory = searchCategory.replaceAll( " ", "" );
        if ( "전체".equals( searchCategory) )
            searchCategory = null;
        String searchAddress = params.get( "filter3" );
//        if ( "전체".equals( searchAddress) )
//            searchAddress = null;
        if ( null != searchAddress && !searchAddress.isEmpty() ) {
            final int input = Integer.parseInt(searchAddress);
            final String[] options = {null, "서구", "중구", "동구","북구", "남구"};
            if (input >= 0 && input < options.length) {
                searchAddress = options[input];
            } else {
                searchAddress = null;
            }
        }

        if ( null != filter1 && !filter1.isEmpty() )
        {
            switch (filter1) {
                case "1": //-> System.out.println( 13 );
                case "2": //-> System.out.println( 13 );
                case "3": //-> System.out.println( 13 );
                {
                    final Map<String, String> sortStrategy = Map.of(
                            "1","restScore2"
                            , "2","totalReviews"
                            , "3","reserveCount");
                    Matcher matcher = Pattern.compile(
                            "([123])"
                    ).matcher( filter1 );

                    List<String> mySort = new ArrayList<>();
                    while (matcher.find()) {
                        mySort.add( sortStrategy.get( matcher.group() ));
                    }

                    Specification<DinningReviewView> spec = Specification
                            .where(DinningReviewSpecifications.likeRestName( restName ))
                            .and(DinningReviewSpecifications.eqCategory( searchCategory ))
                            .and(DinningReviewSpecifications.likeAddr( searchAddress ))
                            ;

                    Pageable pageable = PageRequest.of( 0, pageSize, Sort.Direction.DESC, mySort.toArray(new String[0]) );

                    dinningReviewList =
                            inRepository.findAll( spec, pageable ).toList();
//                    org.thymeleaf.spring6.expression.Fields
                }
                break;
                default :// System.out.println("Unexpected value: " + filter1);
            }
        }
        // 임시
        else //if ( null != searchCategory )
            dinningReviewList = inRepository.findAll( Specification
                    .where(DinningReviewSpecifications.likeRestName( restName ))
                    .and(DinningReviewSpecifications.eqCategory( searchCategory ))
                    .and(DinningReviewSpecifications.likeAddr( searchAddress )) );

        return dinningReviewList;
    }

    @GetMapping("/search")
    public String findRestName(@RequestParam(name = "keyword", defaultValue="") String restName, Model model,
                               @RequestParam Map<String, String> params) {
        List<Dinning> restList = new ArrayList<>();
        boolean bActionDefault = true;

        System.out.println( params );
        List<DinningReviewView> dinningReviewList = searchMain( restName, params, 100000 , dinningWithReviewRepository);
        if ( null != dinningReviewList && !dinningReviewList.isEmpty() )
        {
            bActionDefault = false;

            model.addAttribute("list", dinningReviewList);
        }

        if ( bActionDefault  )
        {
//            restList = dinningRepository.findByRestNameContaining(restName);
            model.addAttribute("list", restList);
        }


//        if ( true )
//        {
//            testService.saveAll();
//        }
//        else {
//            OAuth2LoginUser test = new OAuth2LoginUser();
//            test.setHashValue(123);
//            OAuth2LoginUser result = o2o2o2o2o2o.save(test);
//            System.out.println(result);
//        }


        return "search";
    }
}

