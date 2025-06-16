package com.caden.campcircle.model.dto.postComment;

import com.caden.campcircle.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
public class PostCommentQueryRequest extends PageRequest implements Serializable  {
    private Long postId;
}
