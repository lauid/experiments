package demo1

import "google.golang.org/grpc"

type grpcServer struct {
	createProduct grpc.Handler
	findProduct   grpc.Handler
	removeProduct grpc.Handler
}
func (s *grpcServer) CreateProduct(ctx context.Context, req *pb.ProductRequest) (*pb.ProductResponse, error) {
	_, resp, err := s.createProduct.ServeGRPC(ctx, req)
	if err != nil {
		return nil, err
	}
	return resp.(*pb.ProductResponse), nil
}
func (s *grpcServer) FindProduct(ctx context.Context, req *pb.ProductRequest) (*pb.ProductResponse, error) {
	_, resp, err := s.findProduct.ServeGRPC(ctx, req)
	if err != nil {
		return nil, err
	}
	return resp.(*pb.ProductResponse), nil
}
func (s *grpcServer) RemoveProduct(ctx context.Context, req *pb.ProductRequest) (*pb.ProductResponse, error) {
	_, resp, err := s.removeProduct.ServeGRPC(ctx, req)
	if err != nil {
		return nil, err
	}
	return resp.(*pb.ProductResponse), nil
}
func NewGRPCServer(_ context.Context, endpoint endpoint.ProductEndpoints) pb.ProductServiceServer {
	return &grpcServer{
		createProduct: grpc.NewServer(
			endpoint.CreateEndpoint,
			DecodeGRPCProductRequest,
			EncodeGRPCProductResponse,
		),
		findProduct: grpc.NewServer(
			endpoint.FindEndpoint,
			DecodeGRPCProductRequest,
			EncodeGRPCProductResponse,
		),
		removeProduct: grpc.NewServer(
			endpoint.RemoveEndpoint,
			DecodeGRPCProductRequest,
			EncodeGRPCProductResponse,
		),
	}
}
