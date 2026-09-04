import asyncio
import importlib.util
import inspect
import json
import os
import sys
import traceback
from typing import Any, Protocol, Never


def write_error(msg) -> Never:
    """
    将错误信息写入文件并退出程序，以便 Java 调用方从文件提取错误信息（从 stdout 难以提取）

    错误信息应该用户友好，会直接展示给用户
    """
    with open('error.txt', 'w', encoding='utf-8') as f:
        f.write(msg)
    sys.exit(1)


if not importlib.util.find_spec('playwright'):
    write_error('Python 环境未安装 playwright 模块')

from playwright.async_api import async_playwright, Browser


def extract_exception_location(exc):
    """从异常提取报错的代码行号"""
    tb = exc.__traceback__
    while tb is not None:
        filename = tb.tb_frame.f_code.co_filename
        if os.path.basename(filename) == 'script.py':
            return tb.tb_lineno
        tb = tb.tb_next
    return ''


def read_input() -> tuple[str, dict[str, Any], list[str]]:
    """解析入参"""
    if not os.path.exists('in.json'):
        write_error('脚本入参文件 in.json 不存在')
    with (open('in.json', encoding='utf-8') as f):
        args: dict[str, Any] = json.load(f)
        if not args.get('cdp_url'):
            write_error('脚本入参中 cdp_url 不能为空')
        return args['cdp_url'], args.get('params', {}), args.get('files', [])


def write_output(result):
    """将返回值写入文件"""
    with open('out.json', 'w', encoding='utf-8') as out_f:
        try:
            json.dump(result, out_f, default=str)
        except Exception as e:
            traceback.print_exc()
            write_error('序列化脚本返回值失败: ' + str(e))


class ScriptMethod(Protocol):
    """浏览器自动化脚本 main 方法的协议"""

    async def __call__(self, browser: Browser,
                       params: dict[str, Any] = ...,
                       files: list[str] = ...) -> (dict[str, Any] | None):
        """
        执行浏览器自动化操作

        :param browser: playwright 浏览器实例
        :param params: 脚本参数，可选
        :param files: 上传的文件列表，可选。表示本地文件路径
        :return: 脚本的出参，第一级只能包含 downloaded_files, result. 可选
        """
        ...


def load_script_method() -> tuple[ScriptMethod, tuple[str, ...]]:
    """加载脚本中的 main 方法"""
    try:
        from script import main as script_main
    except SyntaxError as e:
        # 脚本语法错误
        # 打印异常堆栈到 stderr 以便排查问题
        traceback.print_exc()
        write_error(f"脚本语法错误. 行: {e.lineno}, 列: {e.offset}, 错误: {e.msg}, 代码: {e.text}")
    except Exception as e:
        # 脚本未定义 main 方法
        if isinstance(e, ImportError) and "'main'" in str(e):
            traceback.print_exc()
            write_error('脚本未定义 main 方法')
        else:
            traceback.print_exc()
            write_error('加载脚本失败: ' + str(e))

    if not inspect.iscoroutinefunction(script_main):
        write_error('main 方法未定义为 async')

    signature = inspect.signature(script_main)
    arg_names = tuple(signature.parameters.keys())
    if not set(arg_names).issubset({'browser', 'params', 'files', 'file'}):
        write_error(
            'main 方法参数不合法，参数名称只能包含 browser, params, files, file, 实际为 ({", ".join(arg_names)})')
    return script_main, arg_names


async def invoke_script_method(script_main: ScriptMethod, arg_names: tuple[str, ...], cdp_url: str,
                               params: dict[str, Any],
                               files: list[str]) -> dict[str, Any] | None:
    """调用脚本的方法"""
    # playwright 修复了问题但尚未发布 1.58.0 版本，暂时通过环境变量屏蔽警告
    # https://github.com/microsoft/playwright/issues/38469
    os.environ['NODE_OPTIONS'] = '--no-deprecation'
    async with async_playwright() as p:
        try:
            browser = await p.chromium.connect_over_cdp(cdp_url)
        except Exception as e:
            if 'connect_over_cdp: Unexpected status 404' in str(e):
                write_error('沙箱不存在，请重新创建')
            else:
                traceback.print_exc()
                write_error('连接沙箱失败: ' + str(e))

        try:
            args = []
            for arg_name in arg_names:
                if arg_name == 'browser':
                    args.append(browser)
                elif arg_name == 'params':
                    args.append(params)
                elif arg_name == 'files':
                    args.append(files)
                elif arg_name == 'file':
                    args.append(files[0] if len(files) > 0 else None)
                else:
                    args.append(None)
            return await script_main(*args)
        except Exception as e:
            traceback.print_exc()
            lineno = extract_exception_location(e)
            # 异常堆栈从 script.py 开始，忽略前面的 wrapper.py 帧；忽略最后一帧（跟 str(e) 有重复）
            stack_trace = ''.join(traceback.format_exception(e)[2:-1])
            # 删除异常堆栈中的目录路径（减少安全风险）
            stack_trace = stack_trace.replace(os.path.abspath(os.curdir) + '/', '').rstrip()
            write_error(f'执行脚本失败, 行: {lineno}, 异常: {str(e)}\n{stack_trace}')

        finally:
            await browser.close()


def main():
    cdp_url, params, files = read_input()
    script_main, arg_names = load_script_method()
    try:
        result = asyncio.run(invoke_script_method(script_main, arg_names, cdp_url, params, files))
        if result:
            if not isinstance(result, dict):
                write_error(f"脚本返回值错误，应为 dict 类型，实际为 {type(result)}")
            write_output(result)
    except Exception as e:
        traceback.print_exc()
        write_error(f'执行脚本失败, {str(e)}')


main()
