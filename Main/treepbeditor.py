# coding:utf-8
import logging

LOG_FORMAT = "%(asctime)s - %(levelname)s - %(message)s"


class LLog(object):
    _enable = False

    @staticmethod
    def enable(enable):
        LLog._enable = enable

    @staticmethod
    def log(*my_args):
        logging.basicConfig(filename='treepbeditor.log', level=logging.DEBUG, format=LOG_FORMAT)
        logging.error(my_args)
        if LLog._enable:
            print(my_args)
        return


import os
import sys

curpypath = os.path.split(os.path.realpath(__file__))[0]
sys.path.append(curpypath + '/../..')
os.chdir(curpypath)
sys.path.append(os.getcwd())

import xlwings as xw
import wx
import wx.lib.gizmos as gizmos  # Formerly wx.gizmos in Classic
import wx.lib.intctrl
import wx.adv

LLog.log(sys.version)

import conv2pb
from google.protobuf import text_format
from inspect import currentframe, getframeinfo

# ----------------------------------------------------------------------

class PbNode(object):
    def __init__(self, fieldDesc, msgData, viewNode, nodeParent):
        self.msgData = msgData
        self.viewNode = viewNode
        self.fieldDesc = fieldDesc
        self.nodeParent = nodeParent
        # self.childNodes = {}
        self.asRepeatedNode = False
        self.debugName = ''
        # print dir(self.viewNode)
        self.viewNode.SetData(self)

        if fieldDesc is not None:
            self.debugName = fieldDesc.name

    def AddChildNode(self, childNode):
        return childNode

    def ResetMsgData(self, msgData):
        if self.msgData is not None:
            raise "Can not Reset None msgData"
        self.msgData = msgData


# ----------------------------------------------------------------------

class TestPanel(wx.Panel):
    def __init__(self, parent, msg, uppermsg_field_desc):

        wx.Panel.__init__(self, parent, -1)

        winids = []

        # A window to the left of the client window
        self.leftWindow1 = wx.adv.SashLayoutWindow(
            self, -1, wx.DefaultPosition, (200, 30),
            wx.NO_BORDER | wx.adv.SW_3D
        )

        self.leftWindow1.SetDefaultSize((720, 1000))
        self.leftWindow1.SetOrientation(wx.adv.LAYOUT_VERTICAL)
        self.leftWindow1.SetAlignment(wx.adv.LAYOUT_LEFT)

        self.leftWindow1.SetSashVisible(wx.adv.SASH_RIGHT, True)
        self.leftWindow1.SetExtraBorderSize(10)

        winids.append(self.leftWindow1.GetId())

        self.tree = gizmos.TreeListCtrl(self.leftWindow1, -1, style=0, agwStyle=
        gizmos.TR_DEFAULT_STYLE
        | gizmos.TR_HAS_BUTTONS
        # | gizmos.TR_TWIST_BUTTONS
        | gizmos.TR_ROW_LINES
        # | gizmos.TR_COLUMN_LINES
        # | gizmos.TR_NO_LINES
        # | gizmos.TR_LINES_AT_ROOT
        | gizmos.TR_FULL_ROW_HIGHLIGHT
        | gizmos.TR_HAS_VARIABLE_ROW_HEIGHT
                                        )

        # create some columns
        self.tree.AddColumn("Key")
        self.tree.AddColumn("Desc")
        self.tree.AddColumn("Value")
        # self.tree.RemoveColumn(3)
        self.tree.SetMainColumn(0)  # the one with the tree in it...
        self.tree.SetColumnWidth(0, 225)
        self.tree.SetColumnWidth(1, 175)
        self.tree.SetColumnWidth(2, 375)
        # self.tree.SetColumnEditable(2, True)

        # if hasattr(msg, 'DESCRIPTOR'):
        des = msg.DESCRIPTOR
        LLog.log(type(msg))
        self.root = self.tree.AddRoot(des.name)
        self.rootMsg = msg
        self.rootPbNode = PbNode(None, msg, self.root, None)
        self.ProcSubMsg(self.rootMsg, self.rootPbNode, upperFieldDesc=uppermsg_field_desc, showSettedOnly=True)
        # else:
        #     LLog.log(dir(msg))
        #     self.root = self.tree.AddRoot(msg._message_descriptor.name)
        #     self.rootMsg = msg
        #     self.rootPbNode = PbNode(None, msg, self.root, None)
        #     self.ProcField(self.rootMsg, self.rootPbNode, showSettedOnly=True)
        #     # field, realPbParentNode, field_key, v, oneof2GroupMap, group2OneofMap, procUninit = False):
        #     # self.ProcField(self.rootMsg, self.rootPbNode, showSettedOnly=True)

        self.tree.Expand(self.root)

        self.tree.GetMainWindow().Bind(wx.EVT_RIGHT_UP, self.OnRightUp)
        self.tree.Bind(wx.EVT_TREE_ITEM_ACTIVATED, self.OnActivate)

        self.remainingSpace = wx.Panel(self, -1, style=wx.SUNKEN_BORDER)
        # self.edit_text = wx.TextCtrl(self.remainingSpace, style=wx.TE_MULTILINE|wx.TE_RICH2|wx.HSCROLL)
        self.staticText = wx.StaticText(self.remainingSpace, -1, str(self.rootMsg), (5, 5))
        self.staticTextActiveViewNode = self.root

        self.Bind(
            wx.adv.EVT_SASH_DRAGGED_RANGE, self.OnSashDrag,
            id=min(winids), id2=max(winids)
        )

        self.Bind(wx.EVT_SIZE, self.OnSpSize)
        self.ExpandSubTree(self.root)

    def ChoiceOneOf(self, pbNode1):
        def OnChoice(event):
            choose = event.GetString()
            choose = choose.split(':')[0]
            LLog.log("choice " + choose)
            self.tree.DeleteChildren(pbNode1.viewNode)
            # pbNode1.ClearAllChildrenNode()

            value1 = pbNode1.nodeParent.msgData.__getattribute__(choose)

            oneofField1 = None
            for oneof in pbNode1.fieldDesc.fields:
                if oneof.name == choose:
                    oneofField1 = oneof
                    break

            # print 'oneofField1:' + str(oneofField1)
            self.ProcField(oneofField1, pbNode1, oneofField1.name, value1, {}, {})

            # print self.rootMsg

            self.UpdateParentMsgStr(pbNode1)
            self.MarkInited(pbNode1)
            self.ExpandSubTree(pbNode1.viewNode)

        return OnChoice

    def ChoiceEnum(self, pbNode1):
        def OnChoice(event):
            choose = event.GetString()
            choose = choose.split(':')[0]
            LLog.log("ChoiceEnum " + choose)
            enum = pbNode1.fieldDesc.enum_type.values_by_name[choose].number
            pbNode1.nodeParent.msgData.__setattr__(pbNode1.fieldDesc.name, enum)

            self.UpdateParentMsgStr(pbNode1)
            self.MarkInited(pbNode1)

        return OnChoice

    def AddRepeated(self, pbNode1):
        def OnAdd(event):
            button = event.GetEventObject()
            msg = pbNode1.msgData.add()
            # print dir(v)
            idx = len(pbNode1.msgData) - 1
            arChild = self.tree.AppendItem(pbNode1.viewNode, '[' + str(idx) + ']')
            # self.tree.SetItemText(arChild, str(msg).replace('\n', ' ').replace('\r', ''), 2)
            arPbNode = pbNode1.AddChildNode(PbNode(pbNode1.fieldDesc, msg, arChild, pbNode1))

            buttonRemove = wx.Button(self.tree.GetMainWindow(), -1, "remove")
            buttonRemove.Bind(wx.EVT_BUTTON, self.RemoveRepeated(arPbNode))
            self.tree.SetItemWindow(arChild, buttonRemove, 2)

            self.ProcSubMsg(msg, arPbNode)
            # print self.rootMsg

            self.UpdateParentMsgStr(arPbNode)
            self.MarkInited(arPbNode)
            self.ExpandSubTree(pbNode1.viewNode)

        return OnAdd

    def GetMsgItemIndex(self, pbNode1):
        for idx, item in enumerate(pbNode1.nodeParent.msgData):
            if item is pbNode1.msgData:
                return idx
        return -1

    def RemoveRepeated(self, pbNode1):

        def OnRemove(event):
            button = event.GetEventObject()
            # msg = pbNode1.msgData.add()
            # print idx
            idx = self.GetMsgItemIndex(pbNode1)
            # print 'remove:'+str(idx)
            del pbNode1.nodeParent.msgData[idx]
            self.tree.Delete(pbNode1.viewNode)

            if self.tree.ItemHasChildren(pbNode1.nodeParent.viewNode):
                child, cookie = self.tree.GetFirstChild(pbNode1.nodeParent.viewNode)
                childIdx = 0
                while child:
                    child.SetText(None, '[' + str(childIdx) + ']')
                    (child, cookie) = self.tree.GetNextChild(pbNode1.nodeParent.viewNode, cookie)
                    childIdx += 1

            # print self.rootMsg

            self.UpdateParentMsgStr(pbNode1.nodeParent)
            self.MarkInited(pbNode1.nodeParent)
            # self.ExpandSubTree(pbNode1.viewNode)

        return OnRemove

    def ChangeSingle(self, pbNode1):
        def OnChange(event):
            ctl = event.GetEventObject()
            value = ctl.GetValue()
            pbNode1.nodeParent.msgData.__setattr__(pbNode1.fieldDesc.name, value)
            # print self.rootMsg
            # print "change:"+pbNode1.debugName
            self.UpdateParentMsgStr(pbNode1)
            self.MarkInited(pbNode1)

        return OnChange

    def ChangeNativeRepeated(self, pbNode1):
        def OnChange(event):
            ctl = event.GetEventObject()
            value = ctl.GetValue()
            value = value[1:][:-1]
            # print value
            varr = value.split(',')

            repeated = pbNode1.msgData
            while len(repeated) > 0:
                repeated.pop()

            vstr = ''
            for v in varr:
                vstr += (pbNode1.fieldDesc.name + ' : ' + v + '\n')
            # print repeated, vstr
            text_format.Merge(vstr, pbNode1.nodeParent.msgData)
            # print self.rootMsg

            self.UpdateParentMsgStr(pbNode1)
            self.MarkInited(pbNode1)

        return OnChange

    def UpdateParentMsgStr(self, pbNode1):
        self.staticText.SetLabel(str(self.staticTextActiveViewNode.GetData().msgData))

        # if pbNode1.nodeParent is None:
        #     print 'UpdateParentMsgStr null child :'+pbNode1.debugName
        #     return
        # node1 = pbNode1.nodeParent
        # print 'UpdateParentMsgStr check:'+node1.debugName
        # if node1.fieldDesc is not None: # and  or node1.fieldDesc.message_type is not None
        #     print 'UpdateParentMsgStr update:'+node1.debugName
        #     self.tree.SetItemText(node1.viewNode, str(node1.msgData).replace('\n', ' ').replace('\r', ''), 2)
        #
        # self.UpdateParentMsgStr(node1)

    def MarkInited(self, pbNode):
        # print 'MarkInited update:'+pbNode.debugName
        self.tree.SetItemTextColour(pbNode.viewNode, wx.Colour(0, 0, 0))
        if pbNode.nodeParent is not None:
            self.MarkInited(pbNode.nodeParent)

    def ExpandSubTree(self, viewNode):
        """ expand all nodes """
        self.tree.Expand(viewNode)
        fn = self.tree.Expand
        self.traverse(viewNode, fn)

    def OnCollapseAll(self, viewNode):
        """ collapse all nodes of the tree """
        fn = self.tree.Collapse
        self.traverse(viewNode, fn)

    def traverse(self, traverseroot, function):
        """ recursivly walk tree control """
        # step in subtree if there are items or ...
        if self.tree.ItemHasChildren(traverseroot):
            child, cookie = self.tree.GetFirstChild(traverseroot)
            while child:
                function(child)
                self.traverse(child, function)
                (child, cookie) = self.tree.GetNextChild(traverseroot, cookie)

    def ProcField(self, field, realPbParentNode, field_key, v, oneof2GroupMap, group2OneofMap, procUninit=False):

        ResKeywords_pb2 = conv2pb.MOD['ResKeywords.proto']
        if field.label == field.LABEL_REPEATED:
            field_key += "[]"

        if field.message_type is not None:
            field_key += "{}"

        if field.name in oneof2GroupMap.keys():
            if oneof2GroupMap[field.name].name not in group2OneofMap.keys():
                oneofGroupField = oneof2GroupMap[field.name]
                child = self.tree.AppendItem(realPbParentNode.viewNode, oneofGroupField.name)
                # button = wx.Button(self.tree.GetMainWindow(), -1, "reset "+oneofGroupField.name)
                pbNode = realPbParentNode.AddChildNode(
                    PbNode(oneofGroupField, realPbParentNode.msgData, child, realPbParentNode))
                realPbParentNode = pbNode

                sampleList = []
                cIdx = 0
                for nIdx, oneofField in enumerate(oneofGroupField.fields):
                    sample = oneofField.name + ':'
                    if field.has_options and oneofField.GetOptions().HasExtension(ResKeywords_pb2.editorDesc):
                        sample += oneofField.GetOptions().Extensions[ResKeywords_pb2.editorDesc]
                    sampleList.append(sample)
                    if oneofField.name == field.name:
                        cIdx = nIdx

                button = wx.Choice(self, -1, (100, 50), choices=sampleList)
                button.SetSelection(cIdx)
                self.tree.SetItemWindow(child, button, 2)

                if oneofGroupField.has_options and oneofGroupField.GetOptions().HasExtension(
                        ResKeywords_pb2.editorOneofDesc):
                    self.tree.SetItemText(child,
                                          oneofGroupField.GetOptions().Extensions[ResKeywords_pb2.editorOneofDesc], 1)

                self.Bind(wx.EVT_CHOICE, self.ChoiceOneOf(pbNode), button)
                group2OneofMap[oneofGroupField.name] = None
            if procUninit:
                return

        if field.has_options and field.GetOptions().HasExtension(ResKeywords_pb2.editorHide):
            if field.GetOptions().Extensions[ResKeywords_pb2.editorHide]:
                LLog.log('skip:' + field.name)
                return

        child = self.tree.AppendItem(realPbParentNode.viewNode, field_key)
        pbNode = realPbParentNode.AddChildNode(PbNode(field, v, child, realPbParentNode))
        if procUninit:
            self.tree.SetItemTextColour(child, wx.Colour(128, 128, 128))

        # print dir(field)
        # print conv2pb.MOD

        if field.has_options and field.GetOptions().HasExtension(ResKeywords_pb2.editorDesc):
            self.tree.SetItemText(child, field.GetOptions().Extensions[ResKeywords_pb2.editorDesc], 1)

        # self.tree.SetItemText(child, str(field.type), 1)

        if field.message_type is not None:

            # self.tree.SetItemText(child, str(v).replace('\n', ' ').replace('\r', ''), 2)
            if field.label != field.LABEL_REPEATED:
                # message类型
                self.ProcSubMsg(v, pbNode)
            else:
                # if field.name in oneofMap
                # message repeated类型
                button = wx.Button(self.tree.GetMainWindow(), -1, "add")
                # print button

                button.Bind(wx.EVT_BUTTON, self.AddRepeated(pbNode))

                self.tree.SetItemWindow(child, button, 2)

                for idx, item in enumerate(v):
                    arChild = self.tree.AppendItem(child, '[' + str(idx) + ']')
                    arPbNode = pbNode.AddChildNode(PbNode(field, item, arChild, pbNode))
                    buttonRemove = wx.Button(self.tree.GetMainWindow(), -1, "remove")
                    buttonRemove.Bind(wx.EVT_BUTTON, self.RemoveRepeated(arPbNode))
                    self.tree.SetItemWindow(arChild, buttonRemove, 2)
                    # self.tree.SetItemText(arChild, str(item).replace('\n', ' ').replace('\r', ''), 2)
                    self.ProcSubMsg(item, arPbNode)

        else:
            # 原生类型，无论数组还是field，都直接单行显示ʾ

            if field.label != field.LABEL_REPEATED:
                if field.type in [field.TYPE_INT32, field.TYPE_INT64, field.TYPE_SFIXED32, field.TYPE_SFIXED64,
                                  field.TYPE_SINT32, field.TYPE_SINT64, field.TYPE_UINT32, field.TYPE_UINT64]:
                    target_ctl = wx.SpinCtrl(self.tree.GetMainWindow(), -1, value=str(v), pos=(75, 50), min=-999999999,
                                             max=9999999999)
                    self.tree.SetItemWindow(child, target_ctl, 2)
                    target_ctl.Bind(wx.EVT_SPINCTRL, self.ChangeSingle(pbNode))

                elif field.type in [field.TYPE_DOUBLE, field.TYPE_FLOAT]:
                    target_ctl = wx.SpinCtrlDouble(self.tree.GetMainWindow(), -1, value=str(v), pos=(75, 50), inc=1.0,
                                                   min=-999999999, max=9999999999)
                    self.tree.SetItemWindow(child, target_ctl, 2)
                    target_ctl.Bind(wx.EVT_SPINCTRLDOUBLE, self.ChangeSingle(pbNode))

                elif field.type in [field.TYPE_STRING]:

                    width = len(v)
                    width = width if width > 8 else 8
                    target_ctl = wx.TextCtrl(self.tree.GetMainWindow(), -1, value=v, size=(16 * width, -1))
                    self.tree.SetItemWindow(child, target_ctl, 2)
                    # print field.name + ':' + v + '|'
                    target_ctl.Bind(wx.EVT_TEXT, self.ChangeSingle(pbNode))
                elif field.type in [field.TYPE_ENUM]:
                    # print dir(field)
                    # print dir(v)
                    # print field.enum_type
                    sampleList = []
                    cIdx = 0
                    # print field.name
                    for idx, eField in enumerate(field.enum_type.values):
                        # print dir(eField.number)
                        # print v
                        if eField.number == v:
                            cIdx = idx
                            # print "match!!:"
                            # print eField.name
                            # print idx

                        sample = eField.name + ':'
                        if eField.has_options and eField.GetOptions().HasExtension(ResKeywords_pb2.name):
                            sample += eField.GetOptions().Extensions[ResKeywords_pb2.name]

                        sampleList.append(sample)
                    button = wx.Choice(self, -1, (100, 50), choices=sampleList)
                    button.SetSelection(cIdx)
                    self.tree.SetItemWindow(child, button, 2)
                    self.Bind(wx.EVT_CHOICE, self.ChoiceEnum(pbNode), button)

                else:
                    self.tree.SetItemText(child, str(v) + ' unsupport', 2)
            elif field.label == field.LABEL_REPEATED:
                strv = str(v)
                width = len(strv)
                width = width if width > 8 else 8
                target_ctl = wx.TextCtrl(self.tree.GetMainWindow(), -1, value=strv, size=(16 * width, -1))
                self.tree.SetItemWindow(child, target_ctl, 2)
                target_ctl.Bind(wx.EVT_TEXT, self.ChangeNativeRepeated(pbNode))

    def ProcSubMsg(self, msg, pbNodeParent, upperFieldDesc=None, showSettedOnly=False):
        oneof2GroupMap = {}
        group2OneofMap = {}
        fieldSettedMap = {}

        if showSettedOnly and len(msg._fields) == 0:
            # 如果是跟节点，并且没有设置任何field，则说明是一个新增空cell，所以这里需要显示备选字段给配表人员
            showSettedOnly = False
            if upperFieldDesc.label == upperFieldDesc.LABEL_REPEATED:
                # 当前新增的cell是一个数组，所以需要特殊处理

                if hasattr(msg, upperFieldDesc.name):
                    realMsg = getattr(msg, upperFieldDesc.name)
                    self.ProcField(upperFieldDesc, pbNodeParent, upperFieldDesc.name, realMsg, oneof2GroupMap, {})
                    return

        for k, v in msg._oneofs.items():
            oneof2GroupMap[v.name] = k
            group2OneofMap[k.name] = v

        for field, v in msg._fields.items():
            # if subNode is not None and subNode != field.name:
            #     continue
            fieldSettedMap[field.name] = None
            field_key = field.name

            self.ProcField(field, pbNodeParent, field_key, v, oneof2GroupMap, {})

        if showSettedOnly:
            return

        descOneof2GroupMap = {}

        for oneof in msg.DESCRIPTOR.oneofs:
            for onefield in oneof.fields:
                descOneof2GroupMap[onefield.name] = oneof
                # print oneof.name, onefield.name, onefield

        for field in msg.DESCRIPTOR.fields:
            # print field.name, field, field.type, field.message_type, field.label
            # if subNode is not None and subNode != field.name:
            #     continue

            if field.name in fieldSettedMap.keys():
                continue

            field_key = field.name
            value = msg.__getattribute__(field.name)

            self.ProcField(field, pbNodeParent, field_key, value, descOneof2GroupMap, group2OneofMap, True)

    def OnSashDrag(self, event):
        if event.GetDragStatus() == wx.adv.SASH_STATUS_OUT_OF_RANGE:
            LLog.log('drag is out of range')
            return

        eobj = event.GetEventObject()

        if eobj is self.leftWindow1:
            LLog.log('leftwin1 received drag event')
            self.leftWindow1.SetDefaultSize((event.GetDragRect().width, 1000))

        wx.adv.LayoutAlgorithm().LayoutWindow(self, self.remainingSpace)
        self.remainingSpace.Refresh()

    def OnSpSize(self, event):
        wx.adv.LayoutAlgorithm().LayoutWindow(self, self.remainingSpace)
        LLog.log("OnSpSize:" + str(self.GetSize()))

    def OnActivate(self, evt):
        LLog.log('OnActivate: %s %s' % (self.tree.GetItemText(evt.GetItem()), evt.GetItem().GetData()))
        self.staticText.SetLabel(str(evt.GetItem().GetData().msgData))
        self.staticTextActiveViewNode = evt.GetItem()

    def OnRightUp(self, evt):
        pos = evt.GetPosition()
        item, flags, col = self.tree.HitTest(pos)
        if item:
            LLog.log('Flags: %s, Col:%s, Text: %s' %
                     (flags, col, self.tree.GetItemText(item, col)))

    def OnSize(self, evt):
        self.tree.SetSize(self.GetSize())
        LLog.log(self.GetSize())


import re


def editorApp(msg, uppermsg_field_desc):
    app = wx.App()
    frm = wx.Frame(None, title="PbEditor", size=(960, 500))
    # msg = genePbMsg()
    pnl = TestPanel(frm, msg, uppermsg_field_desc)
    frm.Show()
    app.MainLoop()


@xw.func
def treeview(address, context):
    # LLog.log(address, context)
    # LLog.log(xw.Range(address).column)
    meta_cell = xw.Range((3, xw.Range(address).column)).value
    if meta_cell is None or meta_cell.endswith('}'):
        return

    convert_rule_text = xw.Range((1, 1)).value
    contents = re.split(r'([\s\S]*?)convert\(([\s\S]*),([\s\S]*),([\s\S]*)\)([\s\S]*)', convert_rule_text);
    protofile_name = contents[2].strip()
    # LLog.log(protofile_name)
    # print meta_cell, convert_rule_text, protofile_name
    # print curpypath

    # load_module_spe_path(os.path.splitext(protofile_name)[0], '../pb_pyout')
    # LLog.log(os.getcwd())
    module = conv2pb.compile_module_with_import(protofile_name, xw.sheets.active.name, conv2pb.RAW_IMPORT_PATH,
                                                conv2pb.RAW_PYTHON_PATH, '../../pb_pyout', conv2pb.MOD)
    # LLog.log( conv2pb.MOD)
    config_name = contents[3].strip()
    # print config_name
    if not config_name in module.__dict__.keys():
        print('sheet[%s] proto[%s] not found, dont need conv ?' % (xw.sheets.active.name, config_name))
        return

    table = module.__dict__[config_name]()
    prototype = module.DESCRIPTOR.message_types_by_name[config_name]
    row_field = prototype.fields[0]
    # print prototype, row_field
    key = meta_cell
    value = xw.Range(address).value
    # print key, value, row_field.name

    row_objs = getattr(table, row_field.name)
    row_obj = row_objs.add()
    repeated_map = {}

    uppermsg_field_desc = conv2pb.load_fieldDesc(row_field, key)
    if uppermsg_field_desc.message_type is None:
        return

    if value is None:
        value = ''

    one_obj = conv2pb.load_struct('', module, '', row_field, row_obj, key, value, repeated_map, '', '')
    # print '----------------------'
    LLog.log(one_obj)
    # print '----------------------'
    # print dir(one_obj)
    # print dir(one_obj)
    # print (one_obj._fields.keys()[0].name)
    # print (one_obj._fields.values()[0].DESCRIPTOR.name)
    # print one_obj.DESCRIPTOR.name

    editorApp(one_obj, uppermsg_field_desc)
    LLog.log(str(one_obj))
    xw.Range(address).value = str(one_obj)


def oneofFieldChoice(row_field, key, selection):
    value = selection.value
    key_split_array = key.split('&')

    oneof_uppermsg_name = key_split_array[0][:-1]
    oneof_name = key_split_array[1]

    select_oneof_field_name = value
    # print type(row_field), dir(row_field)
    # print type(row_obj.DESCRIPTOR), dir(row_obj.DESCRIPTOR)
    # print oneof_uppermsg_name

    oneof_uppermsg_field_desc = conv2pb.load_fieldDesc(row_field, oneof_uppermsg_name)
    oneof_uppermsg_name_desc = oneof_uppermsg_field_desc.message_type
    # select_oneof_msg_desc = oneof_uppermsg_name_desc.fields_by_name[select_oneof_field_name].message_type

    # print oneof_uppermsg_field_desc.name
    # print dir(oneof_uppermsg_name_desc)
    oneof_group = oneof_uppermsg_name_desc.oneofs_by_name[oneof_name]
    # print oneof_group
    # print dir(oneof_group)
    # print oneof_group.fields
    sampleList = []
    for field in oneof_group.fields:
        sampleList.append(field.name)

    print
    sampleList
    try:
        f = selection.api.Validation.Formula1
    except:
        # 没有设置过
        selection.api.Validation.Add(3, 2, 3, ','.join(sampleList))
    else:
        selection.api.Validation.Modify(3, 2, 3, ','.join(sampleList))


@xw.func
def autoTip(address, context):
    convert_rule_text = xw.Range((1, 1)).value
    LLog.log(xw.sheets.active.name + " start")

    #
    # xw.sheets.active.data_validation('H10', {'validate': 'integer',
    #                              'criteria': 'not between'})

    # val = xw.Range('H10')
    # print val
    # val.api.Validation.add(3, 1, 3, "linear_benefit,linear_cost,sigmoid_benefit,sigmoid_cost")
    #
    # return

    selection = xw.apps.active.selection

    all_columns = xw.sheets.active.used_range.columns.count

    if selection.row <= 3 or selection.row > xw.sheets.active.used_range.rows.count:
        return

    convert_rule_text = xw.Range((1, 1)).value
    contents = re.split(r'([\s\S]*?)convert\(([\s\S]*),([\s\S]*),([\s\S]*)\)([\s\S]*)', convert_rule_text);
    protofile_name = contents[2].strip()

    module = conv2pb.compile_module_with_import(protofile_name, xw.sheets.active.name, conv2pb.RAW_IMPORT_PATH,
                                                conv2pb.RAW_PYTHON_PATH,
                                                '../../pb_pyout', conv2pb.MOD)
    config_name = contents[3].strip()

    if not config_name in module.__dict__.keys():
        print('sheet[%s] proto[%s] not found, dont need conv ?' % (xw.sheets.active.name, config_name))
        return

    table = module.__dict__[config_name]()
    prototype = module.DESCRIPTOR.message_types_by_name[config_name]
    row_field = prototype.fields[0]
    ResKeywords_pb2 = conv2pb.MOD['ResKeywords.proto']

    LLog.log("import finished:" + str(all_columns))

    # LLog.log(xw.Range((3, 1), (3, all_columns)).raw_value)
    # LLog.log(type(xw.Range((3, 1), (3, all_columns)).raw_value))

    keys_row = xw.Range((3, 1), (3, all_columns)).raw_value[0]
    LLog.log(keys_row)
    # xw.Range((1, 7), (1, 8)).raw_value = ('aa', 'bb')
    for col in range(all_columns):
        key_cell = keys_row[col]
        # print key_cell
        if key_cell is not None and '&' in key_cell:
            LLog.log("key_cell:" + str(key_cell) + ' ' + str(col))
            key_split_array = key_cell.split('&')

            oneof_uppermsg_name = key_split_array[0][:-1]

            select_oneof_field_name = xw.Range((selection.row, col + 1)).value
            LLog.log(oneof_uppermsg_name, select_oneof_field_name)

            # print row_field.name, type(row_field), config_name

            # oneof_uppermsg_name_desc = row_field.message_type.fields_by_name[oneof_uppermsg_name].message_type
            oneof_uppermsg_name_desc = conv2pb.load_fieldDesc(row_field, oneof_uppermsg_name).message_type
            tips_list = []
            oneofkey_list = []
            if select_oneof_field_name is not None:
                select_oneof_msg_field_desc = oneof_uppermsg_name_desc.fields_by_name[select_oneof_field_name]
                select_oneof_msg_desc = select_oneof_msg_field_desc.message_type
                # print oneof_uppermsg_name_desc.name, select_oneof_msg_desc.name

                if select_oneof_msg_field_desc.has_options and select_oneof_msg_field_desc.GetOptions().HasExtension(
                        ResKeywords_pb2.editorDesc):
                    # xw.Range((2, col+1)).raw_value = select_oneof_msg_field_desc.GetOptions().Extensions[ResKeywords_pb2.editorDesc]
                    tips_list.append(select_oneof_msg_field_desc.GetOptions().Extensions[ResKeywords_pb2.editorDesc])
                    # print 1, col+1, select_oneof_msg_field_desc.GetOptions().Extensions[ResKeywords_pb2.editorDesc]

                LLog.log("load_fieldDesc:")
                tips_row = xw.Range((1, col + 2), (1, all_columns)).raw_value[0]
                for column in range(col + 2, all_columns):
                    tip_cell = tips_row[column - (col + 2)]
                    if tip_cell is not None and tip_cell.startswith(key_cell + '.&'):
                        oneof_subfiled_num = tip_cell.split('.')[-1][1:]
                        if not oneof_subfiled_num.isnumeric():
                            raise ('not oneof subfield number match:' + tip_cell)

                        if int(oneof_subfiled_num) == 1 and select_oneof_msg_desc is None:
                            if select_oneof_msg_field_desc.has_options and select_oneof_msg_field_desc.GetOptions().HasExtension(
                                    ResKeywords_pb2.editorDesc):
                                tips_list.append(
                                    select_oneof_msg_field_desc.GetOptions().Extensions[ResKeywords_pb2.editorDesc])
                            else:
                                tips_list.append('')
                            select_rule = oneof_uppermsg_name + '.' + select_oneof_field_name
                            oneofkey_list.append(select_rule)
                        elif select_oneof_msg_desc is not None and int(
                                oneof_subfiled_num) in select_oneof_msg_desc.fields_by_number:
                            # print select_oneof_msg_desc.fields_by_number[int(oneof_subfiled_num)].name
                            oneof_field = select_oneof_msg_desc.fields_by_number[int(oneof_subfiled_num)]

                            select_rule = oneof_uppermsg_name + '.' + select_oneof_field_name + '.' + oneof_field.name

                            # print oneof_field, select_rule
                            if oneof_field.has_options and oneof_field.GetOptions().HasExtension(
                                    ResKeywords_pb2.editorDesc):
                                # xw.Range((2, column)).raw_value = oneof_field.GetOptions().Extensions[ResKeywords_pb2.editorDesc]
                                tips_list.append(oneof_field.GetOptions().Extensions[ResKeywords_pb2.editorDesc])
                            else:
                                tips_list.append('')
                            # xw.Range((3, column)).raw_value = select_rule
                            oneofkey_list.append(select_rule)
                            # sheet.put_cell(rule_row, select_rule_col, xlrd.XL_CELL_TEXT, select_rule, 0)
                            #
                            # print rule_row, select_rule_col
                            # print sheet.cell_value(rule_row, select_rule_col)
                        else:
                            tips_list.append('')
                            oneofkey_list.append('')
                    else:
                        break
            else:
                tips_row = xw.Range((1, col + 2), (1, all_columns)).raw_value[0]
                for column in range(col + 2, all_columns):
                    tip_cell = tips_row[column - (col + 2)]
                    if tip_cell is not None and tip_cell.startswith(key_cell + '.&'):
                        tips_list.append('')
                        oneofkey_list.append('')
                    else:
                        break
                LLog.log("tip fill:", len(tips_list), col)
                # tips_list = [''] * (all_columns - col - 1)
                # oneofkey_list = [''] * (all_columns - col - 2)

            LLog.log("tip write:" + str(len(tips_list)))
            xw.Range((2, col + 1), (2, col + len(tips_list))).raw_value = tuple(tips_list)
            xw.Range((3, col + 2), (3, col + 1 + len(oneofkey_list))).raw_value = tuple(oneofkey_list)
            # break

    LLog.log("oneof title tip finished")
    row_objs = getattr(table, row_field.name)
    row_obj = row_objs.add()
    repeated_map = {}
    key = xw.Range((3, selection.column)).value
    value = selection.value

    if key is None:
        return

    # LLog.log(conv2pb, module, row_field, row_obj, key, value, repeated_map)
    #
    cell_fieldDesc = conv2pb.load_fieldDesc(row_field, key)
    LLog.log("load_fieldDesc finished")
    if cell_fieldDesc is None:
        LLog.log(cell_fieldDesc, key)
        if '&' in key:
            oneofFieldChoice(row_field, key, selection)
            LLog.log("oneofFieldChoice finished")
            return
        else:
            return

    if cell_fieldDesc.enum_type is not None and cell_fieldDesc.label != cell_fieldDesc.LABEL_REPEATED and len(cell_fieldDesc.enum_type.values)<30:
        sampleList = []
        # if cell_fieldDesc.enum_type is module.ResKeywords__pb2._GAMEBUFFERID:
        #     for idx, eField in enumerate(cell_fieldDesc.enum_type.values):
        #         if eField.has_options and eField.GetOptions().HasExtension(ResKeywords_pb2.buffProp):
        #             sample = eField.GetOptions().Extensions[ResKeywords_pb2.name]
        #             sampleList.append(sample)
        for idx, eField in enumerate(cell_fieldDesc.enum_type.values):
            if eField.has_options and eField.GetOptions().HasExtension(ResKeywords_pb2.name):
                sample = eField.GetOptions().Extensions[ResKeywords_pb2.name]
                sampleList.append(sample)
            # else:
            #     sample = eField.name  # + ':'

        editSheet = xw.sheets.active
        enumSheet = None
        LLog.log(xw.sheets)
        shs = []
        for sh in xw.sheets:
            shs.append(sh.name)

        if '#enumHelper' in shs:
            enumSheet = xw.sheets['#enumHelper']
        else:
            enumSheet = xw.sheets.add('#enumHelper')

        enumSheet.api.Visible = False
        enumSheet.clear()
        enumSheet.range((1, 1), (1, len(sampleList))).raw_value = tuple(sampleList)
        enumSheet.autofit('c')
        addr = xw.Range((1, len(sampleList))).address

        formula1 = '=\'#enumHelper\'!$A$1:{}'.format(addr)

        try:
            ff = selection.api.Validation.Formula1
        except:
            # 没有设置过
            selection.api.Validation.Add(3, 2, 3, formula1)
        else:
            selection.api.Validation.Modify(3, 2, 3, formula1)

    else:
        selection.api.Validation.Delete()


def genePbMsg():
    protofile_name = "ResTestCase.proto"
    config_name = "table_ResTestCaseData"
    module = conv2pb.compile_module_with_import(protofile_name, xw.sheets.active.name, conv2pb.RAW_IMPORT_PATH,
                                                conv2pb.RAW_PYTHON_PATH,
                                                '../../pb_pyout', conv2pb.MOD)
    table = module.__dict__[config_name]()
    prototype = module.DESCRIPTOR.message_types_by_name[config_name]
    row_field = prototype.fields[0]
    row_objs = getattr(table, row_field.name)
    row_obj = row_objs.add()

    msg = row_obj  # ResTestCase_pb2.ResTestCase()
    msg.id = 1
    msg.chksum = 'EDABBBDDEF'

    msg.pos1.x = 1
    msg.pos1.y = 2
    # msg.pos2.x = 1
    # msg.pos2.y = 2
    # msg.pos3.x = 1
    # msg.pos3.y = 2
    #
    msg.intlist1.extend([10, 11, 12, 13])
    # msg.intlist2.extend([10,11,12,13,10,11,12,13])
    #
    pos = msg.poslist1.add()
    pos.x = 22
    pos.y = 23
    #
    # pos = msg.poslist1.add()
    # pos.x = 32
    # pos.y = 33
    #
    # city = msg.citylist.add()
    # city.cityname = 'chengdu'
    # pos = city.citypos.add()
    # pos.x = 122
    # pos.y = 123
    # pos = city.citypos.add()
    # pos.x = 222
    # pos.y = 223
    #
    #
    # res = msg.reslist.add()
    # res.name = 'timi'
    # res.pos.x = 321
    # res.pos.y = 4321
    # res.value.extend([997,996,995,994])
    # spe = res.spe.add()
    # spe.spekey = 'shanghai'
    # pos = spe.spepos.add()
    # pos.x = 122
    # pos.y = 123
    # pos = spe.spepos.add()
    # pos.x = 222
    # pos.y = 223
    # spe.spevalue.extend([9971,9961,1995,1994])
    # spe = res.spe.add()
    # spe.spekey = 'hangzhou'
    # pos = spe.spepos.add()
    # pos.x = 1232
    # pos.y = 1213
    # pos = spe.spepos.add()
    # pos.x = 2622
    # pos.y = 2283
    #
    #
    # res = msg.reslist.add()
    # res.name = 'adpe'
    # res.pos.x = 1321
    # res.pos.y = 34321
    # res.value.extend([9972,9962,9925,9294])
    # spe = res.spe.add()
    # spe.spekey = 'guangzhou'
    # pos = spe.spepos.add()
    # pos.x = 1422
    # pos.y = 1523
    # pos = spe.spepos.add()
    # pos.x = 2322
    # pos.y = 2623
    # spe = res.spe.add()
    # spe.spekey = 'dongguan'
    # pos = spe.spepos.add()
    # pos.x = 14322
    # pos.y = 15423
    # pos = spe.spepos.add()
    # pos.x = 23252
    # pos.y = 26623
    # spe.spevalue.extend([91971,99261,13995,19494])
    #
    msg.cond.suba.level = 999

    return msg


def xlsTestCase():
    # print 'aa'
    xw.Book(u'C_测试用例扩展.xlsm').set_mock_caller()
    # print 'aa'
    treeview('$X$4', 'condA{ level: 1 count:2 }')  #


def xlsTestCaseAutoTip():
    xw.Book(u'C_测试用例扩展.xlsm').set_mock_caller()
    autoTip(None, None)


def testPbCase():
    msg = genePbMsg()
    editorApp(msg)


if __name__ == '__main__':
    LLog.enable(True)
    # Expects the Excel file next to this source file, adjust accordingly.
    # xlsTestCase()
    # testPbCase()
    # xlsTestCase()
    # xlsTestCaseAutoTip()
else:
    LLog.enable(False)
