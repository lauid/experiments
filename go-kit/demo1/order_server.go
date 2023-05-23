package demo1

import "google.golang.org/grpc"

type grpcServer struct {
	createOrder grpc.Handler
	findOrder   grpc.Handler
	cancelOrder grpc.Handler
}
func (s *grpcServer) CreateOrder(ctx context.Context, req *pb.OrderRequest) (*pb.OrderResponse, error) {
	_, resp, err := s.createOrder.ServeGRPC(ctx, req)
	if err != nil {
		return nil, err
	}
	return resp.(*pb.OrderResponse), nil
}
func (s *grpcServer) FindOrder(ctx context.Context, req *pb.OrderRequest) (*pb.OrderResponse, error) {
	_, resp, err := s.findOrder.ServeGRPC(ctx, req)
	if err != nil {
		return nil, err
	}
	return resp.(*pb.OrderResponse), nil
}
func (s *grpcServer) CancelOrder(ctx context.Context, req *pb.OrderRequest) (*pb.OrderResponse, error) {
	_, resp, err := s.cancelOrder.ServeGRPC(ctx, req)
	if err != nil {
		return nil, err
	}
	return resp.(*pb.OrderResponse), nil
}
func NewGRPCServer(_ context.Context, endpoint endpoint.OrderEndpoints) pb.OrderServiceServer {
	return &grpcServer{
		createOrder: grpc.NewServer(
			endpoint.CreateEndpoint,
			DecodeGRPCOrderRequest,
			EncodeGRPCOrderResponse,
		),
		findOrder: grpc.NewServer(
			endpoint.FindEndpoint,
			DecodeGRPCOrderRequest,
			EncodeGRPCOrderResponse,
		),
		cancelOrder: grpc.NewServer(
			endpoint.CancelEndpoint,
			DecodeGRPCOrderRequest,
			EncodeGRPCOrderResponse,
		),
	}
}
