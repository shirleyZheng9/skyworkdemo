import json
import os
import sys
import traceback

# 进程退出状态码
# 脚本语法错误
SYNTAX_ERROR = 100
# 脚本未定义 invoke 方法
NO_INVOKE_METHOD = 101
# invoke 方法的参数不匹配
INVOKE_METHOD_PARAMS_MISMATCH = 102
# 导入脚本失败
IMPORT_FAILED = 103
# 调用脚本失败
INVOKE_FAILED = 104
# 序列化返回值失败
SERIALIZE_RETURN_VALUE_FAILED = 105


# 将错误信息写入文件，以便 Java 调用方从文件提取错误信息（从 stdout 难以提取）
# 错误信息应该用户友好，会直接展示给用户
def write_error(msg):
    with open('error.txt', 'w', encoding='utf-8') as f:
        f.write(msg)


# 从异常提取报错的代码行号
def extract_exception_location(exc):
    tb = exc.__traceback__
    while tb is not None:
        filename = tb.tb_frame.f_code.co_filename
        if os.path.basename(filename) == 'script.py':
            return tb.tb_lineno
        tb = tb.tb_next
    return ''


# 解析入参。没有入参文件时表示没有入参
if os.path.exists('in.json'):
    with open('in.json', encoding='utf-8') as f:
        args = json.load(f)
else:
    args = []

# 获取 invoke 方法
try:
    from script import invoke
except SyntaxError as e:
    # 脚本语法错误
    # 打印异常堆栈到 stderr 以便排查问题
    traceback.print_exc()
    write_error(f"Python 脚本语法错误. 行: {e.lineno}, 列: {e.offset}, 错误: {e.msg}, 代码: {e.text}")
    sys.exit(SYNTAX_ERROR)
except Exception as e:
    # 脚本未定义 invoke 方法
    if isinstance(e, ImportError) and "'invoke'" in str(e):
        traceback.print_exc()
        sys.exit(NO_INVOKE_METHOD)
    else:
        traceback.print_exc()
        write_error('导入 Python 脚本失败: ' + str(e))
        sys.exit(IMPORT_FAILED)

# 调用 invoke 方法
try:
    result = invoke(*args)
except Exception as e:
    if isinstance(e, TypeError) and str(e).startswith('invoke() '):
        traceback.print_exc()
        write_error('invoke 方法参数不匹配: ' + str(e))
        sys.exit(INVOKE_METHOD_PARAMS_MISMATCH)
    else:
        traceback.print_exc()
        lineno = extract_exception_location(e)
        # 异常堆栈从 script.py 开始，忽略前面的 wrapper.py 帧；忽略最后一帧（跟 str(e) 有重复）
        stack_trace = ''.join(traceback.format_exception(e)[2:-1])
        # 删除异常堆栈中的目录路径（减少安全风险）
        stack_trace = stack_trace.replace(os.path.abspath(os.curdir) + '/', '')
        write_error(f'执行 Python 脚本失败, 行: {lineno}, 异常: {str(e)}\n{stack_trace}')
        sys.exit(INVOKE_FAILED)

# 将返回值写入文件。没有返回值时不写文件
if result:
    with open('out.json', 'w', encoding='utf-8') as f:
        try:
            json.dump(result, f, default=str)
        except Exception as e:
            traceback.print_exc()
            write_error('序列化 Python 脚本返回值失败: ' + str(e))
            sys.exit(SERIALIZE_RETURN_VALUE_FAILED)
