$(function () {
    // 글 삭제 버튼
    $("#btnDel").click(function () {
        let answer = confirm("삭제하시겠습니까?");
        answer && $("form[name='frmDelete']").submit();
    });

    const id = $("input[name='id']").val().trim();
    //현재 글의 id 값( 삭제기능쪽의 input name = 'id' 인 value값을 가져옴)

    // 현재 글의 댓글들을 불러온다
    loadComment(id);

    // 댓글 작성 버튼 누르면 댓글 등록 하기.
    // 1. 어느글에 대한 댓글인지? --> 위에 id 변수에 담겨있다
    // 2. 어느 사용자가 작성한 댓글인지? --> logged_id 값
    // 3. 댓글 내용은 무엇인지?  --> 아래 content

    $('#btn_comment').click(function () {
        const content = $('#input_comment').val().trim();

        // 검증
        if (!content) {
            alert('댓글 입력을 하세요.');
            $('#input_comment').focus();
            return;
        }

        //전달할 parameter 준비 (post)
        const data = {
            "post_id": id,
            "user_id": logged_id,
            "content": content,
        };

        $.ajax({
            url: "/comment/write",
            type: "POST",
            data: data,
            cache: false,
            success: function (data, status) {
                if (status == "success") {
                    if (data.status !== "OK") {
                        alert(data.status);
                        return;
                    }
                    loadComment(id);
                    // 댓글목록 다시 업데이트.
                    $('#input_comment').val('');
                }
            },
        });
    });

});

function loadComment(post_id) {

    $.ajax({
        url: "/comment/list/" + post_id,
        type: "GET",
        cache: false,
        success: function (data, status) {
            if (status == "success") {
                if (data.status !== "OK") {
                    //서버쪽 에러 메세지 있는 경우
                    alert(data.status);
                    return;
                }

                buildComment(data); // 댓글 화면 렌더링

                addDelete();
                // ★댓글목록을 불러오고 난뒤에 삭제에 대한 이벤트 리스너를 등록해야 한다
            }
        },
    });
}

function buildComment(result) {
    $("#cmt_cnt").text(result.count);

    const out = [];
    result.data.forEach(comment => {
        let id = comment.id;
        let content = comment.content.trim();
        let regdate = comment.regdate;

        let user_id = parseInt(comment.user.id);
        let username = comment.user.username;
        let name = comment.user.name;

        // 삭제여부 버튼
        const delBtn = (logged_id !== user_id) ? '' : `
            <i class="btn fa-solid fa-delete-left text-danger" data-bs-toggle="tooltip" data-cmtdel-id="${id}"  title="삭제"></i>
        `
        const row = `
            <tr>
                <td><span><strong>${username}</strong><br><small class="text-secondary">(${name})</small></span></td>
                <td>
                    <span>${content}</span>${delBtn}
                </td>
                <td><span><small class="text-secondary">${regdate}</small></span></td>
            </tr>
        `;
        out.push(row);
    });
    $('#cmt_list').html(out.join('/n'));
}

// 댓글 삭제

function addDelete() {
    const id = $("input[name='id']").val().trim();

    $("[data-cmtdel-id]").click(function () {
        if (!confirm("댓글을 삭제하시겠습니까?")) return;

        const comment_id = $(this).attr("data-cmtdel-id");

        $.ajax({
                url: "/comment/delete",
                type: "POST",
                caches: false,
                data: {"id" : comment_id},
                success: function (data, status) {
                    if (status == "success") {
                        if (data.status !== "OK") {
                            alert(data.status);
                            return;
                        }
                        loadComment(id);
                    }
                }
            }
        );
    })
}

