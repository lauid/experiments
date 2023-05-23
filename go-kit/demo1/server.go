package demo1

import "google.golang.org/grpc"

type grpcServer struct {
	registerUser grpc.Handler
	findUser     grpc.Handler
	deleteUser  grpc.Handler
}
func (s *grpcServer) RegisterUser(ctx context.Context, req *pb.UserRequest) (*pb.UserResponse, error) {
	_, resp, err := s.registerUser.ServeGRPC(ctx, req)
	if err != nil {
		return nil, err
	}
	return resp.(*pb.UserResponse), nil
}
func (s *grpcServer) FindUser(ctx context.Context, req *pb.UserRequest) (*pb.UserResponse, error) {
	_, resp, err := s.findUser.ServeGRPC(ctx, req)
	if err != nil {
		return nil, err
	}
	return resp.(*pb.UserResponse), nil
}
func (s *grpcServer) DeleteUser(ctx context.Context, req *pb.UserRequest) (*pb.UserResponse, error) {
	_, resp, err := s.deleteUser.ServeGRPC(ctx, req)
	if err != nil {
		return nil, err
	}
	return resp.(*pb.UserResponse), nil
}
func NewGRPCServer(_ context.Context, endpoint endpoint.UserEndpoints) pb.UserServiceServer {
	return &grpcServer{
		registerUser: grpc.NewServer(
			endpoint.RegisterEndpoint,
			DecodeGRPCUserRequest,
			EncodeGRPCUserResponse,
		),
		findUser: grpc.NewServer(
			endpoint.FindEndpoint,
			DecodeGRPCUserRequest,
			EncodeGRPCUserResponse,
		),
		deleteUser: grpc.NewServer(
			endpoint.DeleteEndpoint,
			DecodeGRPCUserRequest,
			EncodeGRPCUserResponse,
		),
	}
}
