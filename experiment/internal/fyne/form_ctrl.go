package fyne

import (
	"fmt"
	"fyne.io/fyne/v2"
	"fyne.io/fyne/v2/dialog"
	"fyne.io/fyne/v2/widget"
	"github.com/pkg/errors"
	"google.golang.org/grpc"
	"log"
	"strings"
	"sync"
)

type FormCtrl struct {
	//grpc服务地址
	grpcAddr string

	//结果区域
	ResultLabel *widget.Label
	//窗口对象
	Win fyne.Window
	//输入框
	//InputEntry   *widget.Entry
	InputEntries []*widget.Entry
	//提交
	SubmitButton *widget.Button
	//取消
	CancelButton *widget.Button
	IsProcessing bool

	//进度条
	Progresses   []fyne.CanvasObject
	ProgressLock sync.RWMutex

	//打开工装箱按钮
	OpenBoxButton *widget.Button
	//关闭工装箱按钮
	CloseBoxButton *widget.Button

	//升级次数统计
	CountLabel *widget.Label
}

func NewFormCtrl() *FormCtrl {
	f := &FormCtrl{
		ResultLabel: widget.NewLabel(""),
		CountLabel:  widget.NewLabel(""),
		Progresses:  make([]fyne.CanvasObject, 4),
		grpcAddr:    "localhost:8000",
	}

	//进度条
	for i := 0; i < 4; i++ {
		progress := widget.NewProgressBar()
		progress.Max = 100 // 设置进度的最大值
		f.Progresses[i] = progress
	}

	return f
}

// 更新进度值
func (f *FormCtrl) updateProgress(index uint32, progress uint32) {
	f.ProgressLock.Lock()
	object := f.Progresses[index].(*widget.ProgressBar)
	object.SetValue(float64(progress))
	f.ProgressLock.Unlock()
}

func (f *FormCtrl) SubmitTap() func() {
	return func() {
		if f.IsProcessing {
			err := errors.New("当前有任务正在进行中，不能重复提交，如果要终止请点击取消")
			dialog.ShowError(err, f.Win)
			return
		}

		f.ResultLabel.Text = "" //清空日志输出
		f.IsProcessing = true
		go f.executeGRPCRequest()
	}
}

// 仅供测试使用
func (f *FormCtrl) executeGRPCRequest() {
	defer fmt.Println("grpc req exit..")
	defer func() {
		f.IsProcessing = false
	}()

	//input := f.InputEntry.Text
	var inputs []string
	for _, inputEntry := range f.InputEntries {
		inputs = append(inputs, strings.Trim(inputEntry.Text, ""))
	}
	input := strings.Join(inputs, ",")

	conn, err := grpc.Dial(f.grpcAddr, grpc.WithInsecure())
	if err != nil {
		log.Fatalf("could not connect to server: %v", err)
	}
	defer conn.Close()

	fmt.Println("input: ", input)
	//client := pb.NewHDServiceClient(conn)
	//req := &pb.RequestMessage{Message: input}
	//
	//resStream, err := client.ServerStreamingMethod(context.Background(), req)
	//if err != nil {
	//	log.Fatalf("gRPC request failed: %v", err)
	//}
	//
	//for {
	//	if !f.IsProcessing {
	//		return
	//	}
	//	res, err := resStream.Recv()
	//	if err != nil {
	//		// 处理流接收结束的情况
	//		break
	//	}
	//
	//	// 将结果追加到文本区域
	//	f.SetCount(res.Count)
	//	f.SetResult(fmt.Sprintf("%v\n", res))
	//	f.updateProgress(res.Index, res.Progress)
	//}
}

// SetCount 更新页面的统计次数
func (f *FormCtrl) SetCount(count uint64) {
	if count > 0 {
		f.CountLabel.SetText(fmt.Sprintf("次数：%d", count))
	}
}

// SetResult 将结果追加到文本区域
func (f *FormCtrl) SetResult(msg string) {
	f.ResultLabel.SetText(f.ResultLabel.Text + msg)
}

// ClearResult 清理结果文本区
func (f *FormCtrl) ClearResult() {
	f.ResultLabel.SetText("")
}

// ConfirmCallback 取消dialog 点击
func (f *FormCtrl) ConfirmCallback() func(bool2 bool) {
	return func(response bool) {
		fmt.Println("Responded with", response)
		if response {
			f.IsProcessing = false
		}
	}
}

// OpenBoxTap 打开工装箱子
func (f *FormCtrl) OpenBoxTap() func() {
	return func() {
	}
}

// CloseBoxTap 关闭工装箱子
func (f *FormCtrl) CloseBoxTap() func() {
	return func() {
	}
}
