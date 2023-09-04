package main

import (
	fyne1 "experiment/internal/fyne"
	"fmt"
	fyne "fyne.io/fyne/v2"
	"fyne.io/fyne/v2/app"
	"fyne.io/fyne/v2/canvas"
	"fyne.io/fyne/v2/container"
	"fyne.io/fyne/v2/dialog"
	"fyne.io/fyne/v2/layout"
	"fyne.io/fyne/v2/theme"
	"fyne.io/fyne/v2/widget"
	"github.com/flopp/go-findfont"
	"image/color"
	"log"
	"os"
	"strconv"
	"strings"
)

func main() {
	//main5()
	main3()
}

// 从文件加载资源
func resourceFromFile(filePath string) fyne.Resource {
	file, err := fyne.LoadResourceFromPath(filePath)
	if err != nil {
		panic(err)
	}
	return file
}

// https://puhuiti.oss-cn-hangzhou.aliyuncs.com/AlibabaPuHuiTi.zip
func main3() {
	defer fmt.Println("exit...")
	//设置中文字体
	fontPaths := findfont.List()
	fonts := []string{
		"Alibaba-PuHuiTi-Regular.ttf",
		"msyh.ttf",
		"simhei.ttf",
		"simsun.ttc",
		"simkai.ttf",
	}
LABEL:
	for _, path := range fontPaths {
		for _, font := range fonts {
			if strings.Contains(path, font) {
				os.Setenv("FYNE_FONT", path)
				break LABEL
			}
		}
	}
	defer os.Unsetenv("FYNE_FONT")
	os.Setenv("FYNE_SCALE", "1.5")

	a := app.New()
	a.Settings().SetTheme(theme.LightTheme())
	a.SetIcon(resourceFromFile("assets/logo.png"))

	formCtrl := fyne1.NewFormCtrl()
	formCtrl.Win = a.NewWindow("主控板升级")
	formCtrl.Win.CenterOnScreen()
	//formCtrl.Win.Resize(fyne.NewSize(800, 500))

	// 创建输入框
	for i := 0; i < 4; i++ {
		inputEntry := widget.NewEntry()
		inputEntry.SetPlaceHolder(strconv.Itoa(i) + "请输入条码")
		inputEntry.OnChanged = func(content string) {
			fmt.Println("input:", inputEntry.Text, "entered")
		}
		formCtrl.InputEntries = append(formCtrl.InputEntries, inputEntry)
	}

	//提交按钮
	formCtrl.SubmitButton = widget.NewButton("开  始", formCtrl.SubmitTap())
	formCtrl.OpenBoxButton = widget.NewButton("打开工装箱", formCtrl.OpenBoxTap())
	formCtrl.CloseBoxButton = widget.NewButton("关闭工装箱", formCtrl.CloseBoxTap())
	//取消按钮
	formCtrl.CancelButton = widget.NewButton("取  消", func() {
		cnf := dialog.NewConfirm("Confirmation", "确定取消当前任务?", formCtrl.ConfirmCallback(), formCtrl.Win)
		cnf.SetDismissText("否")
		cnf.SetConfirmText("是")
		cnf.Show()
	})

	//保存文件按钮
	//saveFileButton := widget.NewButton("File Save", func() {
	//	dialog.ShowFileSave(func(writer fyne.URIWriteCloser, err error) {
	//		if err != nil {
	//			dialog.ShowError(err, formCtrl.Win)
	//			return
	//		}
	//		if writer == nil {
	//			log.Println("Cancelled")
	//			return
	//		}
	//
	//		fileSaved(writer, formCtrl.Win)
	//	}, formCtrl.Win)
	//})

	//scale := widget.NewLabel("")
	//screen := widget.NewCard("", "", widget.NewForm(
	//	&widget.FormItem{Text: "当前时间", Widget: scale},
	//	&widget.FormItem{Text: "成功数", Widget: formCtrl.CountLabel},
	//))
	//go func() {
	//	timer := time.NewTicker(1 * time.Second)
	//	defer timer.Stop()
	//	for range timer.C {
	//		scale.SetText(time.Now().Format(time.RFC3339))
	//	}
	//}()

	grid := container.NewAdaptiveGrid(2,
		container.NewVBox(
			formCtrl.InputEntries[0],
			formCtrl.InputEntries[1],
			formCtrl.InputEntries[2],
			formCtrl.InputEntries[3],
		),
		container.NewVBox(formCtrl.Progresses...),
	)
	formGroup := container.NewVBox(
		grid,
		container.NewHBox(
			formCtrl.SubmitButton,
			formCtrl.CancelButton,
			formCtrl.OpenBoxButton,
			formCtrl.CloseBoxButton,
			layout.NewSpacer(),
			formCtrl.CountLabel,
		),
	)

	fixLayout := &fixedSizeLayout{
		width:  800,
		height: 250,
	}
	// 创建布局
	content := container.NewVBox(
		widget.NewCard("", "", formGroup),
		canvas.NewText("日志:", color.RGBA{R: 87, G: 87, B: 87, A: 255}),
		widget.NewCard("", "", container.New(fixLayout, container.NewVScroll(formCtrl.ResultLabel))),
	)

	formCtrl.Win.SetContent(content)
	formCtrl.Win.ShowAndRun()
}

type fixedSizeLayout struct {
	width, height float32
}

func (l *fixedSizeLayout) Layout(objects []fyne.CanvasObject, containerSize fyne.Size) {
	for _, obj := range objects {
		//obj.Resize(fyne.NewSize(containerSize.Width, l.height))
		obj.Resize(fyne.NewSize(containerSize.Width, containerSize.Height))
	}
}

func (l *fixedSizeLayout) MinSize(objects []fyne.CanvasObject) fyne.Size {
	return fyne.NewSize(l.width, l.height)
}

func main5() {
	myApp := app.New()
	myWin := myApp.NewWindow("Entry")
	myWin.Resize(fyne.NewSize(800, 380))

	nameEntry := widget.NewEntry()
	nameEntry.SetPlaceHolder("input name")
	nameEntry.OnChanged = func(content string) {
		fmt.Println("name:", nameEntry.Text, "entered")
	}
	// 创建一个自定义布局，在其中指定输入框的大小
	nameLayout := &fixedSizeLayout{
		width:  400,
		height: 30,
	}
	nameContainer := container.New(nameLayout, nameEntry)
	nameBox := container.NewHBox(widget.NewLabel("Name"), layout.NewSpacer(), nameContainer)

	passEntry := widget.NewPasswordEntry()
	passEntry.SetPlaceHolder("input password")
	passwordBox := container.NewHBox(widget.NewLabel("Password"), layout.NewSpacer(), passEntry)

	loginBtn := widget.NewButton("Login", func() {
		fmt.Println("name:", nameEntry.Text, "password:", passEntry.Text, "login in")
	})

	multiEntry := widget.NewEntry()
	multiEntry.SetPlaceHolder("please enter\nyour description")
	multiEntry.MultiLine = true

	content := container.NewVBox(nameBox, passwordBox, loginBtn, multiEntry)
	myWin.SetContent(content)

	myWin.ShowAndRun()
}

func fileSaved(f fyne.URIWriteCloser, w fyne.Window) {
	defer f.Close()
	_, err := f.Write([]byte("Written by Fyne demo\n"))
	if err != nil {
		dialog.ShowError(err, w)
	}
	err = f.Close()
	if err != nil {
		dialog.ShowError(err, w)
	}
	log.Println("Saved to...", f.URI())
}
