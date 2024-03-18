package com.yi.spring.entity.meta;

import com.yi.spring.entity.ImgTb;
import com.yi.spring.repository.ImgTableRepository;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

public class ImageMan {
    private final String data;
    public ImageMan(String restImg) {
        data = restImg;
        _initIdMap();
    }

    public String getImageData() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Integer, Long> entry : mapId.entrySet()) {
            if ( !sb.isEmpty() )
                sb.append( "," );
            sb.append( entry.getValue() );
        }
        return sb.toString();
    }

    public String setRestImg(ImgTableRepository imageTableRepository, ImageFrom from, byte[] restImg) {
        ImgTb entity = new ImgTb();
        entity.setId( data!=null? Long.valueOf(data) :null );
        entity.setDtype( from.name() );
        entity.setBytes( restImg );
        imageTableRepository.save( entity );

        return String.valueOf(entity.getId());
    }


    public String setReviewImg( ImgTableRepository imageTableRepository, MultipartFile[] files)
    {
        return setReviewImg( imageTableRepository, ImageFrom.REVIEW, files );
    }
    public String setReviewImg( ImgTableRepository imageTableRepository, ImageFrom from, MultipartFile[] files)
    {
        for ( int iIndex = 0; iIndex < files.length; ++iIndex )
            setReviewImg( imageTableRepository, from, files[ iIndex ], iIndex );
        return getImageData();
    }
    public String setReviewImg( ImgTableRepository imageTableRepository, ImageFrom from, MultipartFile file)
    {
        return setReviewImg( imageTableRepository, from, file, 0 );
    }
    public String setReviewImg( ImgTableRepository imageTableRepository, ImageFrom from, MultipartFile file, int iDataIndex )
    {
        ImgTb entity = new ImgTb();
        entity.setId( _findImgId( iDataIndex ) );
        entity.setDtype( from.name() );

        byte[] revImg;
        try {
            revImg = file.getBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        entity.setBytes( revImg );
        imageTableRepository.save( entity );

        mapId.put( iDataIndex, entity.getId() );

        return getImageData();
    }

    private TreeMap< Integer, Long > mapId = new TreeMap<>();
    private void _initIdMap(){
        if ( null == data )
            return;

        String[] strData = data.split( "," );
        for ( int iIndex = 0; iIndex < strData.length; ++iIndex )
        {
            mapId.put( iIndex, Long.valueOf(strData[ iIndex ]));
        }
    }
    private Long _findImgId( int iDataIndex ){
        return mapId.get( iDataIndex );
    }
}

