package logic

import (
	"context"
	"shorturl/rpc/transform/transformer"

	"shorturl/rpc/transform/internal/svc"
	"shorturl/rpc/transform/transform"

	"github.com/zeromicro/go-zero/core/logx"
)

type ShortenLogic struct {
	ctx    context.Context
	svcCtx *svc.ServiceContext
	logx.Logger
}

func NewShortenLogic(ctx context.Context, svcCtx *svc.ServiceContext) *ShortenLogic {
	return &ShortenLogic{
		ctx:    ctx,
		svcCtx: svcCtx,
		Logger: logx.WithContext(ctx),
	}
}

func (l *ShortenLogic) Shorten(in *transform.ShortenReq) (*transform.ShortenResp, error) {
	// todo: add your logic here and delete this line

	//return &transform.ShortenResp{}, nil

	// 手动代码开始
	resp, err := l.svcCtx.Transformer.Shorten(l.ctx, &transformer.ShortenReq{
		Url: in.Url,
	})
	if err != nil {
		return &transform.ShortenResp{}, err
	}

	return &transform.ShortenResp{
		Shorten: resp.Shorten,
	}, nil
	// 手动代码结束

}
