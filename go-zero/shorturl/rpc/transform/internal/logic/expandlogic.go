package logic

import (
	"context"
	"shorturl/rpc/transform/transformer"

	"shorturl/rpc/transform/internal/svc"
	"shorturl/rpc/transform/transform"

	"github.com/zeromicro/go-zero/core/logx"
)

type ExpandLogic struct {
	ctx    context.Context
	svcCtx *svc.ServiceContext
	logx.Logger
}

func NewExpandLogic(ctx context.Context, svcCtx *svc.ServiceContext) *ExpandLogic {
	return &ExpandLogic{
		ctx:    ctx,
		svcCtx: svcCtx,
		Logger: logx.WithContext(ctx),
	}
}

func (l *ExpandLogic) Expand(in *transform.ExpandReq) (*transform.ExpandResp, error) {
	// todo: add your logic here and delete this line

	// 手动代码开始
	resp, err := l.svcCtx.Transformer.Expand(l.ctx, &transformer.ExpandReq{
		Shorten: in.Shorten,
	})
	if err != nil {
		return &transform.ExpandResp{}, err
	}

	return &transform.ExpandResp{
		Url: resp.Url,
	}, nil
	// 手动代码结束

}
