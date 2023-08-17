package demo1

import (
	"context"
	"github.com/go-kit/kit/endpoint"
)

type Endpoints struct {
	GetEndpoint      endpoint.Endpoint
	StatusEndpoint   endpoint.Endpoint
	ValidateEndpoint endpoint.Endpoint
}

func MakeGetEndpoint(srv Service) endpoint.Endpoint {
	return func(ctx context.Context, request interface{}) (response interface{}, err error) {
		_ = request.(getRequest)
		get, err := srv.Get(ctx)
		if err != nil {
			return getResponse{get, err.Error()}, err
		}

		return getResponse{get, ""}, nil
	}
}

func MakeStatusEndpoint(srv Service) endpoint.Endpoint {
	return func(ctx context.Context, request interface{}) (response interface{}, err error) {
		_ = request.(statusRequest)
		d, err := srv.Status(ctx)
		if err != nil {
			return statusResponse{d}, err
		}

		return statusResponse{d}, nil
	}
}

func MakeValidateEndpoint(srv Service) endpoint.Endpoint {
	return func(ctx context.Context, request interface{}) (response interface{}, err error) {
		v := request.(validateRequest)
		b, err := srv.Validate(ctx, v.Date)
		if err != nil {
			return validateResponse{Valid: b, Err: err.Error()}, err
		}

		return validateResponse{b, ""}, nil
	}
}
//
//func (e Endpoints) Get(ctx context.Context) (string, error) {
//	req := getRequest{}
//	resp, err := e.GetEndpoint(ctx, req)
//	if err != nil {
//		return "", err
//	}
//	getResp := resp.(getResponse)
//	if getResp.Err != "" {
//		return "", errors.New(getResp.Err)
//	}
//
//	return getResp.Date, nil
//}
//
//// Status endpoint mapping
//func (e Endpoints) Status(ctx context.Context) (string, error) {
//	req := statusRequest{}
//	resp, err := e.StatusEndpoint(ctx, req)
//	if err != nil {
//		return "", err
//	}
//	statusResp := resp.(statusResponse)
//	return statusResp.Status, nil
//}
//
//// Validate endpoint mapping
//func (e Endpoints) Validate(ctx context.Context, date string) (bool, error) {
//	req := validateRequest{Date: date}
//	resp, err := e.ValidateEndpoint(ctx, req)
//	if err != nil {
//		return false, err
//	}
//	validateResp := resp.(validateResponse)
//	if validateResp.Err != "" {
//		return false, errors.New(validateResp.Err)
//	}
//	return validateResp.Valid, nil
//}
