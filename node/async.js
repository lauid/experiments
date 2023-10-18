// 异步函数调用：

async function fetchData() {
    try {
        const response = await fetch('https://yesno.wtf/api');
        const data = await response.json();
        console.log(data);
    } catch (error) {
        console.error('Error fetching data:', error);
    }
}

// 并行执行多个异步操作：
async function parallelAsyncCalls() {
    try {
        const promise1 = fetch('https://yesno.wtf/api');
        const promise2 = fetch('https://yesno.wtf/api');

        const [result1, result2] = await Promise.all([promise1, promise2]);

        const data1 = await result1.json();
        const data2 = await result2.json();

        console.log(data1, data2);
    } catch (error) {
        console.error('Error fetching data:', error);
    }
}

// 按顺序执行异步操作：
async function sequentialAsyncCalls() {
    try {
        const response1 = await fetch("https://yesno.wtf/api");
        const data1 = await response1.json();
        console.log(data1);

        const response2 = await fetch("https://yesno.wtf/api");
        const data2 = await response2.json();
        console.log(data2);
    } catch (error) {
        console.error("Error fetching data:", error);
    }
}

// fetchData();
// parallelAsyncCalls();

sequentialAsyncCalls();