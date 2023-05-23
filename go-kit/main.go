package main

type ProductService interface {
	GetProduct(id int) (*Product, error)
	CreateOrder(order *Order) error
	PayOrder(id int) error
}

type productService struct {
	repo *productRepo
}
func (s *productService) GetProduct(id int) (*Product, error) {
	return s.repo.GetProduct(id)
}
func (s *productService) CreateOrder(order *Order) error {
	return s.repo.CreateOrder(order)
}
func (s *productService) PayOrder(id int) error {
	return s.repo.PayOrder(id)
}

func main() {

}
