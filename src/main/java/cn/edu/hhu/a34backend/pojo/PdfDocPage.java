package cn.edu.hhu.a34backend.pojo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

@Data
@Document(indexName = "pdfdoc")
public class PdfDocPage
{
    @Id
    @Field(type=FieldType.Long,index=false,store = true)
    private long id;
    @Field(type=FieldType.Long,index=false,store = true)
    private long parentPdfUuid;

    @Field(type=FieldType.Long,index=false,store = true)
    private int pageNumber;

    @Field(type=FieldType.Text,index=true,store = true,analyzer = "ik_smart")
    private String content;

    public PdfDocPage(){}

    public PdfDocPage(long parentPdfUuid,int pageNumber,String content)
    {
        this.parentPdfUuid=parentPdfUuid;
        this.pageNumber=pageNumber;
        this.content=content;
    }



}
